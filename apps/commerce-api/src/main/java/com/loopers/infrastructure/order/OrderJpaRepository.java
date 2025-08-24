package com.loopers.infrastructure.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(String userId);
    long countByStatus(OrderStatus status);
    Optional<Order> findByOrderId(String orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Order o set o.status = :status where o.orderId = :orderId")
    int markOrderFailed(@Param("orderId") String orderId,
                        @Param("status") OrderStatus status);
}
