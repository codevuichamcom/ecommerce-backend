package com.ecommerce.order.domain.model;

import com.ecommerce.common.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderDomainTest {

        private final CustomerId customerId = new CustomerId("cust_123");
        private final String idempotencyKey = "key_123";

        @Test
        @DisplayName("Should correctly calculate total amount from items")
        void create_ShouldCalculateCorrectTotal() {
                // Given
                List<OrderItem> items = List.of(
                                OrderItem.create("p1", "Product 1", 2, new Money(new BigDecimal("10.00"), "USD")),
                                OrderItem.create("p2", "Product 2", 1, new Money(new BigDecimal("25.50"), "USD")));

                // When
                Order order = Order.create(customerId, items, idempotencyKey);

                // Then
                assertThat(order.getTotalAmount().amount()).isEqualByComparingTo("45.50");
                assertThat(order.getStatus()).isInstanceOf(OrderStatus.Pending.class);
        }

        @Test
        @DisplayName("Should transition status correctly")
        void statusTransitions_ShouldWork() {
                // Given
                Order order = Order.create(customerId, List.of(
                                OrderItem.create("p1", "Product 1", 1, Money.of(100))), idempotencyKey);

                // Pending -> Confirmed
                order.confirm();
                assertThat(order.getStatus()).isInstanceOf(OrderStatus.Confirmed.class);

                // Confirmed -> Paid
                order.markAsPaid();
                assertThat(order.getStatus()).isInstanceOf(OrderStatus.Paid.class);
        }

        @Test
        @DisplayName("Should allow cancellation from Pending or Confirmed")
        void cancel_ShouldSucceed_WhenStatusIsAllowed() {
                // From Pending
                Order order1 = Order.create(customerId, List.of(
                                OrderItem.create("p1", "Product 1", 1, Money.of(100))), idempotencyKey);
                order1.cancel("Not needed");
                assertThat(order1.getStatus()).isInstanceOf(OrderStatus.Cancelled.class);

                // From Confirmed
                Order order2 = Order.create(customerId, List.of(
                                OrderItem.create("p1", "Product 1", 1, Money.of(100))), idempotencyKey);
                order2.confirm();
                order2.cancel("Changed mind");
                assertThat(order2.getStatus()).isInstanceOf(OrderStatus.Cancelled.class);
        }

        @Test
        @DisplayName("Should fail cancellation from terminal or paid status")
        void cancel_ShouldThrow_WhenStatusIsDisallowed() {
                // Given
                Order order = Order.create(customerId, List.of(
                                OrderItem.create("p1", "Product 1", 1, Money.of(100))), idempotencyKey);
                order.confirm();
                order.markAsPaid();

                // When & Then
                assertThatThrownBy(() -> order.cancel("Refund"))
                                .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Should throw exception when creating order with empty items")
        void create_ShouldThrow_WhenItemsEmpty() {
                assertThatThrownBy(() -> Order.create(customerId, List.of(), idempotencyKey))
                                .isInstanceOf(ValidationException.class);
        }
}
