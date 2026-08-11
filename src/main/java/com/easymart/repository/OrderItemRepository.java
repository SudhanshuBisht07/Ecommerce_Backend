package com.easymart.repository;

import com.easymart.domain.OrderStatus;
import com.easymart.model.OrderItem;
import com.easymart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProduct(Product product);

    // Used to gate review creation to customers who actually bought the
    // product. Orders in excludedStatuses (PENDING never completed
    // payment, CANCELLED was reversed) don't count as a purchase.
    boolean existsByOrder_User_IdAndProduct_IdAndOrder_OrderStatusNotIn(
            Long userId, Long productId, List<OrderStatus> excludedStatuses);
}
