package com.loopers.domain.order;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderDomainService {

    private final ProductRepository productRepository;
    private final PointRepository pointRepository;

    public OrderDomainService(ProductRepository productRepository,
                              PointRepository pointRepository) {
        this.productRepository = productRepository;
        this.pointRepository = pointRepository;
    }

    public Order createOrder(String userId, List<OrderItem> items) {
        BigDecimal totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST));

            if (product.getStock().getValue() < item.getQuantity()) {
                throw new CoreException(ErrorType.BAD_REQUEST);
            }
        }

        Point point = pointRepository.find(new UserId(userId))
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST));

        if (point.getPointValue().compareTo(totalAmount) < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }

        return new Order(userId, items);
    }
}
