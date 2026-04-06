package com.easymart.repository;

import com.easymart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findBySellerId(Long sellerId);


    //for searching the products based on keywords in name, description or category
    @Query("SELECT p FROM Product p WHERE " +
            ":query IS NULL OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.category.categoryId) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchProduct(@Param("query") String query);
}
