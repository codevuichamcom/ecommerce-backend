package com.ecommerce.order.application.service;

import com.ecommerce.common.exception.ConflictException;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.common.exception.ValidationException;
import com.ecommerce.order.application.dto.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.port.out.InventoryServicePort;
import com.ecommerce.order.application.port.out.InventoryServicePort.ReservationResult;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.domain.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Order application service.
 * Orchestrates order creation with inventory reservation.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductServicePort productService;
    private final InventoryServicePort inventoryService;

    public OrderService(OrderRepository orderRepository,
            ProductServicePort productService,
            InventoryServicePort inventoryService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    /**
     * Create a new order with idempotency support.
     */
    public OrderResponse createOrder(CreateOrderCommand command, String idempotencyKey) {
        log.debug("Creating order for customer {} with {} items (key: {})",
                command.customerId(), command.items().size(), idempotencyKey);

        // 1. Idempotency check - return existing order if already created
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existingOrder.isPresent()) {
                log.info("Returning existing order for idempotency key: {}", idempotencyKey);
                return OrderResponse.from(existingOrder.get());
            }
        }

        // 2. Validate and fetch product details
        List<OrderItem> orderItems = new ArrayList<>();
        for (var itemRequest : command.items()) {
            var product = productService.getProduct(itemRequest.productId());

            if (!product.available()) {
                throw new ValidationException(
                        "productId",
                        String.format("Product '%s' is not available", product.name()));
            }

            orderItems.add(OrderItem.create(
                    product.id(),
                    product.name(),
                    itemRequest.quantity(),
                    new Money(product.price(), product.currency())));
        }

        // 3. Create order in PENDING status
        var order = Order.create(
                new CustomerId(command.customerId()),
                orderItems,
                idempotencyKey);

        // Save order first (so we have an ID for reservation reference)
        var savedOrder = orderRepository.save(order);
        String orderId = savedOrder.getId().value();

        // 4. Reserve inventory for each item
        List<ReservedItem> reservedItems = new ArrayList<>();
        try {
            for (var item : orderItems) {
                var result = inventoryService.reserveStock(
                        item.getProductId(),
                        item.getQuantity(),
                        orderId);

                // Pattern matching on reservation result
                switch (result) {
                    case ReservationResult.Success r ->
                        reservedItems.add(new ReservedItem(item.getProductId(), item.getQuantity()));

                    case ReservationResult.InsufficientStock is -> {
                        throw new ConflictException(
                                "INSUFFICIENT_STOCK",
                                String.format("Insufficient stock for product %s: requested %d, available %d",
                                        item.getProductId(), is.requested(), is.available()));
                    }

                    case ReservationResult.ServiceUnavailable su -> {
                        throw new ConflictException("SERVICE_UNAVAILABLE", su.message());
                    }
                }
            }

            // 5. Confirm order (all reservations successful)
            savedOrder.confirm();
            var confirmedOrder = orderRepository.save(savedOrder);

            log.info("Order {} created and confirmed for customer {}",
                    orderId, command.customerId());

            return OrderResponse.from(confirmedOrder);

        } catch (Exception e) {
            // Rollback on any error
            rollbackReservations(reservedItems, orderId);
            throw e;
        }
    }

    /**
     * Get order by ID.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String id) {
        var order = findOrderOrThrow(id);
        return OrderResponse.from(order);
    }

    /**
     * Get orders by customer ID.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * Cancel an order.
     */
    public OrderResponse cancelOrder(String id, String reason) {
        var order = findOrderOrThrow(id);

        // Release inventory if order was confirmed
        if (order.getStatus() instanceof OrderStatus.Confirmed) {
            for (var item : order.getItems()) {
                try {
                    inventoryService.releaseStock(
                            item.getProductId(),
                            item.getQuantity(),
                            id);
                } catch (Exception e) {
                    log.warn("Failed to release inventory for product {}: {}",
                            item.getProductId(), e.getMessage());
                    // Continue cancellation even if release fails
                }
            }
        }

        order.cancel(reason);
        var cancelledOrder = orderRepository.save(order);

        log.info("Order {} cancelled: {}", id, reason);

        return OrderResponse.from(cancelledOrder);
    }

    private Order findOrderOrThrow(String id) {
        var orderId = new OrderId(id);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

    private void rollbackReservations(List<ReservedItem> reservedItems, String orderId) {
        for (var item : reservedItems) {
            try {
                inventoryService.releaseStock(item.productId(), item.quantity(), orderId);
                log.debug("Released reservation for product {}", item.productId());
            } catch (Exception e) {
                log.error("Failed to release reservation for product {}: {}",
                        item.productId(), e.getMessage());
            }
        }
    }

    private record ReservedItem(String productId, int quantity) {
    }
}
