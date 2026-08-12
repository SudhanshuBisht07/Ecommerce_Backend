package com.easymart.repository;

import com.easymart.domain.OrderStatus;
import com.easymart.model.OrderItem;
import com.easymart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProduct(Product product);

    // Used to gate review creation: a customer can only review a product
    // once their order for it has actually been delivered.
    boolean existsByOrder_User_IdAndProduct_IdAndOrder_OrderStatus(
            Long userId, Long productId, OrderStatus status);
}
