package com.ecommerce.product.infrastructure.seed.factory;

import com.ecommerce.product.infrastructure.seed.config.DataSeedingConfig;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductDataFactoryTest {

    @Test
    void shouldGenerateDeterministicProducts() {
        // Given
        Faker faker = new DataSeedingConfig().faker();
        ProductDataFactory factory = new ProductDataFactory(faker);

        // When
        SeededData data = factory.generateProducts();

        // Then
        assertThat(data.totalCount()).isEqualTo(120);
        // Distribution: 80% active of 120 = 96
        assertThat(data.activeCount()).isEqualTo(96);
        assertThat(data.inactiveCount()).isEqualTo(12);
        assertThat(data.draftCount()).isEqualTo(6);
        assertThat(data.discontinuedCount()).isEqualTo(6);

        assertThat(data.products()).hasSize(120);

        // Check reproducibility
        Faker faker2 = new DataSeedingConfig().faker();
        ProductDataFactory factory2 = new ProductDataFactory(faker2);
        SeededData data2 = factory2.generateProducts();

        assertThat(data.products().get(0).getName())
                .isEqualTo(data2.products().get(0).getName());
        assertThat(data.products().get(0).getPrice().amount())
                .isEqualTo(data2.products().get(0).getPrice().amount());
    }
}
