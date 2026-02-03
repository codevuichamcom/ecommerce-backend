package com.ecommerce.product.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis Cache Configuration following Spring Boot best practices.
 * 
 * Key decisions:
 * - Uses Jackson2JsonRedisSerializer instead of
 * GenericJackson2JsonRedisSerializer
 * to avoid type information (@class field) requirements
 * - Configures ObjectMapper with JavaTimeModule for Java 8+ date/time support
 * - Uses StringRedisSerializer for keys (efficient and debuggable)
 * - Sets appropriate TTL for different cache regions
 */
@Configuration
@EnableCaching
public class CacheConfig {

        /**
         * Creates a custom ObjectMapper for Redis serialization.
         * This is separate from Spring's main ObjectMapper to avoid conflicts.
         */
        @Bean
        public ObjectMapper redisObjectMapper() {
                ObjectMapper mapper = new ObjectMapper();

                // Register JavaTimeModule for Java 8+ date/time types (Instant, LocalDateTime,
                // etc.)
                mapper.registerModule(new JavaTimeModule());

                // Use ISO-8601 format for dates instead of timestamps
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                // Ignore unknown properties during deserialization (forward compatibility)
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);

                return mapper;
        }

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                        ObjectMapper redisObjectMapper) {
                // Create Jackson2JsonRedisSerializer with Object.class
                // This allows caching any type without requiring @class field
                Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper,
                                Object.class);

                // Default cache configuration
                RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)) // Default TTL: 10 minutes
                                .disableCachingNullValues() // Don't cache null values
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(serializer));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                // Product detail cache: 10 minutes (frequently accessed, relatively stable)
                                .withCacheConfiguration("products",
                                                defaultConfig.entryTtl(Duration.ofMinutes(10)))
                                // Product list cache: 5 minutes (changes more frequently)
                                .withCacheConfiguration("product-list",
                                                defaultConfig.entryTtl(Duration.ofMinutes(5)))
                                .build();
        }
}
