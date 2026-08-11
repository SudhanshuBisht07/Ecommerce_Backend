package com.easymart.repository;

import com.easymart.model.Cart;
import com.easymart.model.CartItem;
import com.easymart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    CartItem findByCartAndProductAndSize(Cart cart, Product product, String size);
    List<CartItem> findByProduct(Product product);
}
