package com.ecommerce.inventory.infrastructure.seed.factory;

import com.ecommerce.inventory.domain.model.Inventory;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for generating realistic Inventory entities.
 */
@Component
@Profile("dev")
public class InventoryDataFactory {

    private final Faker faker;

    public InventoryDataFactory(Faker faker) {
        this.faker = faker;
    }

    public List<Inventory> generateInventory(List<String> productIds) {
        List<Inventory> inventories = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            String productId = productIds.get(i);
            int quantity;

            // Distribution:
            // 70% In stock (10-500)
            // 20% Low stock (1-9)
            // 10% Out of stock (0)

            double rand = i / (double) productIds.size();

            if (rand < 0.7) {
                quantity = faker.number().numberBetween(10, 500);
            } else if (rand < 0.9) {
                quantity = faker.number().numberBetween(1, 9);
            } else {
                quantity = 0;
            }

            Inventory inventory = Inventory.create(productId, quantity);

            // Generate some reserved stock for realism (randomly 0-5)
            // But only if we have available stock
            if (quantity > 10) {
                // For seeding, we don't have public API to set reserved stock directly
                // without order reference.
                // We could rely on Inventory.reconstitute if we really wanted fine control,
                // but using create() is cleaner.
                // We'll skip setting reserved stock to keep it simple and consistent.
            }

            inventories.add(inventory);
        }

        return inventories;
    }
}
