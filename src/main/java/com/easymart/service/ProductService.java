package com.easymart.service;

import com.easymart.exceptions.ProductException;
import com.easymart.model.Product;
import com.easymart.model.Seller;
import com.easymart.request.CreateProductRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Product createProduct(CreateProductRequest req, Seller seller);
    void deleteProduct(Long productId) throws ProductException;
    Product updateProduct(Long productId, CreateProductRequest product) throws ProductException;
    Product findProductById(Long productId) throws ProductException;
     List<Product> searchProducts(String query);
    Page<Product> getAllProducts(String category,String brand, String color, String size, Integer minPrice,
                                        Integer maxPrice,Integer minDiscount, String sort, String stock, Integer pageNumber);
    List<Product> getProductBySellerId(Long sellerId);
    List<Product> getFeaturedProducts();
    Product setFeatured(Long productId, boolean featured) throws ProductException;
}
