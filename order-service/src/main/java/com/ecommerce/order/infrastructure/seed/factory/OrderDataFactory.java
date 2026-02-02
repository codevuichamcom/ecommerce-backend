package com.ecommerce.order.infrastructure.seed.factory;

import com.ecommerce.order.domain.model.CustomerId;
import com.ecommerce.order.domain.model.Money;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderId;
import com.ecommerce.order.domain.model.OrderItem;
import com.ecommerce.order.domain.model.OrderStatus;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory for generating realistic Order entities.
 */
@Component
@Profile("dev")
public class OrderDataFactory {

    private final Faker faker;

    public OrderDataFactory(Faker faker) {
        this.faker = faker;
    }

    public List<Order> generateOrders(List<String> productIds, List<String> customerIds) {
        List<Order> orders = new ArrayList<>();

        // Ensure we have some products and customers
        if (productIds.isEmpty() || customerIds.isEmpty()) {
            return orders;
        }

        for (int i = 0; i < 150; i++) {
            orders.add(createOrder(i, productIds, customerIds));
        }

        return orders;
    }

    private Order createOrder(int index, List<String> productIds, List<String> customerIds) {
        // Use real Customer ID from Auth Service
        String customerIdStr = customerIds.get(faker.number().numberBetween(0, customerIds.size()));
        CustomerId customerId = new CustomerId(customerIdStr);

        // Random number of items (1-3 to avoid duplicates logic simplification)
        int itemCount = faker.number().numberBetween(1, 4);
        List<OrderItem> items = new ArrayList<>();

        BigDecimal totalAmountDecimal = BigDecimal.ZERO;

        for (int i = 0; i < itemCount; i++) {
            String productId = productIds.get(faker.number().numberBetween(0, productIds.size()));
            int quantity = faker.number().numberBetween(1, 4);

            // Random price for seeding
            double priceDouble = faker.number().randomDouble(2, 10, 200);
            BigDecimal priceVal = BigDecimal.valueOf(priceDouble);
            Money price = Money.of(priceVal);

            // Fake product name as we don't have it in the list (simplification)
            String productName = faker.commerce().productName();

            items.add(OrderItem.create(productId, productName, quantity, price));

            totalAmountDecimal = totalAmountDecimal.add(priceVal.multiply(BigDecimal.valueOf(quantity)));
        }

        Money totalAmount = Money.of(totalAmountDecimal);
        OrderId orderId = new OrderId(UUID.randomUUID().toString());
        String idempotencyKey = UUID.randomUUID().toString();

        // Distribution of Status
        double rand = faker.number().randomDouble(2, 0, 1);
        OrderStatus status;
        Instant now = Instant.now();
        Instant createdAt = now.minus(faker.number().numberBetween(1, 30), ChronoUnit.DAYS);
        Instant updatedAt = createdAt;

        if (rand < 0.4) {
            status = OrderStatus.Delivered.INSTANCE;
            updatedAt = createdAt.plus(3, ChronoUnit.DAYS);
        } else if (rand < 0.6) {
            status = OrderStatus.Shipped.INSTANCE;
            updatedAt = createdAt.plus(1, ChronoUnit.DAYS);
        } else if (rand < 0.75) {
            status = OrderStatus.Paid.INSTANCE;
            updatedAt = createdAt.plus(1, ChronoUnit.HOURS);
        } else if (rand < 0.85) {
            status = OrderStatus.Confirmed.INSTANCE;
            updatedAt = createdAt.plus(30, ChronoUnit.MINUTES);
        } else if (rand < 0.95) {
            status = OrderStatus.Pending.INSTANCE;
        } else {
            status = new OrderStatus.Cancelled("Customer changed mind");
            updatedAt = createdAt.plus(1, ChronoUnit.HOURS);
        }

        return Order.reconstitute(
                orderId,
                customerId,
                items,
                status,
                totalAmount,
                idempotencyKey,
                createdAt,
                updatedAt);
    }
}
