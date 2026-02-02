package com.ecommerce.product.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                mapper.registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule());
                mapper.activateDefaultTyping(
                                mapper.getPolymorphicTypeValidator(),
                                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
                                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);
                mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(java.util.Objects.requireNonNull(Duration.ofMinutes(10)))
                                .disableCachingNullValues()
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer));

                return RedisCacheManager.builder(java.util.Objects.requireNonNull(connectionFactory))
                                .cacheDefaults(config)
                                .withCacheConfiguration("products",
                                                config.entryTtl(java.util.Objects
                                                                .requireNonNull(Duration.ofMinutes(10))))
                                .withCacheConfiguration("product-list",
                                                config.entryTtl(java.util.Objects
                                                                .requireNonNull(Duration.ofMinutes(5))))
                                .build();
        }
}
