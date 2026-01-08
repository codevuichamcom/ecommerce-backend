package com.ecommerce.inventory.infrastructure.kafka;

import com.ecommerce.common.events.InventoryEvents;
import com.ecommerce.common.outbox.OutboxEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventProducer {

    private final OutboxEventPublisher outboxEventPublisher;

    public InventoryEventProducer(OutboxEventPublisher outboxEventPublisher) {
        this.outboxEventPublisher = outboxEventPublisher;
    }

    public void publishStockReserved(String productId, String orderId, int quantity, int remainingAvailable) {
        InventoryEvents.StockReserved event = InventoryEvents.StockReserved.create(
                productId, orderId, quantity, remainingAvailable);
        outboxEventPublisher.publish("Inventory", productId, event);
    }

    public void publishStockReservationFailed(String productId, String orderId, int requestedQuantity,
            int availableQuantity) {
        InventoryEvents.StockReservationFailed event = InventoryEvents.StockReservationFailed.create(
                productId, orderId, requestedQuantity, availableQuantity);
        outboxEventPublisher.publish("Inventory", productId, event);
    }

    public void publishStockReleased(String productId, String orderId, int quantity, int newAvailable) {
        InventoryEvents.StockReleased event = InventoryEvents.StockReleased.create(
                productId, orderId, quantity, newAvailable);
        outboxEventPublisher.publish("Inventory", productId, event);
    }

    public void publishAllItemsReserved(String orderId) {
        InventoryEvents.AllItemsReserved event = InventoryEvents.AllItemsReserved.create(orderId);
        // Use orderId as aggregateId since this is an aggregate event for the order
        // from inventory perspective
        // Or we could use a dummy ID. But typically Outbox requires AggregateType/ID.
        // Let's use "Order" as aggregate type here or "InventoryReservation"?
        // The consumer expects it to come from Inventory Service.
        outboxEventPublisher.publish("Inventory", "order-" + orderId, event);
    }
}
