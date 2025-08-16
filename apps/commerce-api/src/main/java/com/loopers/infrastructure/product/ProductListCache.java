package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.product.ProductResult;
import com.loopers.domain.product.ProductSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ProductListCache {

    private final StringRedisTemplate redis;   // ✅ 문자열로만 저장/조회
    private final ObjectMapper om;             // 스프링 기본 ObjectMapper 사용

    private static final String PREFIX = "product:list";  // 네임스페이스
    private static final Duration TTL = Duration.ofSeconds(90); // 필요하면 조정

    public String key(ProductSearchCondition c) {
        return PREFIX
                + "::brand:" + (c.getBrandId() != null ? c.getBrandId() : "ALL")
                + ":sort:" + c.getSortType()
                + ":p:" + c.getPage()
                + ":s:" + c.getSize();
    }

    public Optional<List<ProductResult>> get(ProductSearchCondition c) {
        try {
            String k = key(c);
            String json = redis.opsForValue().get(k);
            if (json == null) return Optional.empty();
            List<ProductResult> list = om.readValue(json, new TypeReference<>() {});
            return Optional.of(list);
        } catch (Exception e) {
            // 캐시 장애는 무시하고 미스 처리
            return Optional.empty();
        }
    }

    public void put(ProductSearchCondition c, List<ProductResult> list) {
        if (list == null || list.isEmpty()) return; // 기존 unless와 동일 정책
        try {
            String k = key(c);
            String json = om.writeValueAsString(list);
            redis.opsForValue().set(k, json, withJitter(TTL));
        } catch (Exception ignore) { }
    }

    private Duration withJitter(Duration base) {
        return base.plusSeconds(ThreadLocalRandom.current().nextInt(0, 30));
    }

    public void evictByBrand(Long brandId) {
        try {
            String pattern = PREFIX + "::brand:" + brandId + ":*";
            var conn = redis.getRequiredConnectionFactory().getConnection();
            try (var cursor = conn.scan(org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    byte[] k = cursor.next();
                    conn.keyCommands().del(k);
                }
            }
        } catch (Exception ignore) { }
    }

}
