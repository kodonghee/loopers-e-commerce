package com.loopers.config.redis;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.Properties;

@Configuration
public class RedisHitMetricsConfig {

    @Bean
    public Object redisHitMissGauges(RedisConnectionFactory cf, MeterRegistry registry) {
        Gauge.builder("redis.keyspace.hits", () -> read(cf, "keyspace_hits")).register(registry);
        Gauge.builder("redis.keyspace.misses", () -> read(cf, "keyspace_misses")).register(registry);
        Gauge.builder("redis.keyspace.hit.ratio", () -> {
            double h = read(cf, "keyspace_hits"), m = read(cf, "keyspace_misses");
            double t = h + m;
            return t == 0 ? 0.0 : h / t;
        }).register(registry);
        return new Object();
    }

    private static long read(RedisConnectionFactory cf, String key) {
        Properties p = cf.getConnection().serverCommands().info("stats");
        return Long.parseLong(p.getProperty(key, "0"));
    }
}
