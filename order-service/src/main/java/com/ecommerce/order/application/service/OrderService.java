package com.ecommerce.order.application.service;

import com.ecommerce.common.events.OrderEvents;
import com.ecommerce.common.exception.NotFoundException;
import com.ecommerce.order.application.dto.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.port.out.ProductServicePort;
import com.ecommerce.order.domain.model.*;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.order.domain.saga.OrderSaga;
import com.ecommerce.order.domain.saga.OrderSagaRepository;
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
    private final OrderSagaRepository sagaRepository;
    private final com.ecommerce.common.outbox.OutboxEventPublisher outboxEventPublisher;

    public OrderService(OrderRepository orderRepository,
            ProductServicePort productService,
            OrderSagaRepository sagaRepository,
            com.ecommerce.common.outbox.OutboxEventPublisher outboxEventPublisher) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.sagaRepository = sagaRepository;
        this.outboxEventPublisher = outboxEventPublisher;
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
                throw new com.ecommerce.common.exception.ValidationException(
                        com.ecommerce.common.exception.ErrorCode.PRODUCT_NOT_AVAILABLE, product.name());
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

        var savedOrder = orderRepository.save(order);
        OrderId orderId = savedOrder.getId();

        // 4. Start Order Saga
        var saga = OrderSaga.start(orderId);
        sagaRepository.save(saga);

        // 5. Publish OrderCreated event via Outbox
        publishOrderCreatedEvent(savedOrder);

        log.info("Order creation initiated: {} for customer {}",
                orderId.value(), command.customerId());

        return OrderResponse.from(savedOrder);
    }

    private void publishOrderCreatedEvent(Order order) {
        List<OrderEvents.OrderItemData> itemData = order.getItems().stream()
                .map(item -> new OrderEvents.OrderItemData(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.unitPrice().amount(),
                        item.unitPrice().currency()))
                .toList();

        OrderEvents.OrderCreated event = OrderEvents.OrderCreated.create(
                order.getId().value(),
                order.getCustomerId().value(),
                itemData,
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                order.getIdempotencyKey());

        outboxEventPublisher.publish("Order", order.getId().value(), event);
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

        order.cancel(reason);
        var cancelledOrder = orderRepository.save(order);

        // Publish OrderCancelled event
        publishOrderCancelledEvent(cancelledOrder, reason);

        log.info("Order {} cancelled: {}", id, reason);

        return OrderResponse.from(cancelledOrder);
    }

    private void publishOrderCancelledEvent(Order order, String reason) {
        // requiresRefund is true if order status was confirmed (implying payment was
        // made)
        boolean requiresRefund = order.getStatus() instanceof OrderStatus.Confirmed;

        OrderEvents.OrderCancelled event = OrderEvents.OrderCancelled.create(
                order.getId().value(),
                order.getCustomerId().value(),
                reason,
                requiresRefund);

        outboxEventPublisher.publish("Order", order.getId().value(), event);
    }

    private Order findOrderOrThrow(String id) {
        var orderId = new OrderId(id);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", id));
    }

}
