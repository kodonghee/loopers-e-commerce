package com.loopers.config.redis;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String CACHE_PRODUCT_DETAIL = "product:detail";
    public static final String CACHE_PRODUCT_LIST   = "product:list";

    @Bean
    public RedisCacheManager redisCacheManager(LettuceConnectionFactory cf) {

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .computePrefixWith(name -> name + "::");

        RedisCacheConfiguration detailCfg = base.entryTtl(withJitter(Duration.ofMinutes(30)));
        RedisCacheConfiguration listCfg   = base.entryTtl(withJitter(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(Map.of(
                        CACHE_PRODUCT_DETAIL, detailCfg,
                        CACHE_PRODUCT_LIST,   listCfg
                ))
                .build();
    }

    private Duration withJitter(Duration base) {
        return base.plusSeconds(ThreadLocalRandom.current().nextInt(0, 30));
    }
}
