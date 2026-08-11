package com.easymart.repository;

import com.easymart.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findBySellerIdOrderByOrderDateDesc(Long sellerId);
}
