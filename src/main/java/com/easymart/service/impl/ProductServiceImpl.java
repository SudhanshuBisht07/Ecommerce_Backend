package com.easymart.service.impl;

import com.easymart.service.ProductService;
import com.easymart.exceptions.ProductException;
import com.easymart.model.Category;
import com.easymart.model.Product;
import com.easymart.model.Seller;
import com.easymart.repository.CategoryRepository;
import com.easymart.repository.ProductRepository;
import com.easymart.request.CreateProductRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Product createProduct(CreateProductRequest req, Seller seller) {
        Category category1=categoryRepository.findByCategoryId(req.getCategory());
        if(category1==null){
            Category category=new Category();
            category.setCategoryId(req.getCategory());
            category.setLevel(1);
            category1=categoryRepository.save(category);
        }
        Category category2=categoryRepository.findByCategoryId(req.getCategory2());
        if(category2==null){
            Category category=new Category();
            category.setCategoryId(req.getCategory2());
            category.setLevel(2);
            category.setParentCategory(category1);
            category2=categoryRepository.save(category);
        }
        Category category3=categoryRepository.findByCategoryId(req.getCategory3());
        if(category3==null){
            Category category=new Category();
            category.setCategoryId(req.getCategory3());
            category.setLevel(3);
            category.setParentCategory(category2);
            category3=categoryRepository.save(category);
        }
        Product product= new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setBrand(req.getBrand());
        product.setDescription(req.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setImages(req.getImages());
        product.setSize(req.getSize());
        product.setSellingPrice(req.getSellingPrice());
        product.setMrpPrice(req.getMrpPrice());
        int discountPercentage=calculateDiscountPercentage(req.getMrpPrice().intValue(), req.getSellingPrice().intValue());
        product.setDiscountPercent(discountPercentage);
        return productRepository.save(product);
    }
    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice){
        if(mrpPrice<=0){
            throw new IllegalArgumentException("actual price must be greater than zero ");
        }
        double discount=mrpPrice-sellingPrice;
        if(discount<0){
            throw new IllegalArgumentException("actual price must be greater than selling price");
        }
        double discountPercentage=(discount/mrpPrice)*100;
        return (int)discountPercentage;
    }

    @Override
    public void deleteProduct(Long productId)throws ProductException {
        Product product=findProductById(productId);
        productRepository.delete(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductException {
        Product existingProduct = findProductById(productId);
        if(product.getTitle() != null)
            existingProduct.setTitle(product.getTitle());
        if(product.getBrand() != null)
            existingProduct.setBrand(product.getBrand());
        if(product.getDescription() != null)
            existingProduct.setDescription(product.getDescription());
        if(product.getSellingPrice() != null)
            existingProduct.setSellingPrice(product.getSellingPrice());
        if(product.getMrpPrice() != null)
            existingProduct.setMrpPrice(product.getMrpPrice());
        if(product.getColor() != null)
            existingProduct.setColor(product.getColor());
        if(product.getImages() != null)
            existingProduct.setImages(product.getImages());
        if(product.getSize() != null)
            existingProduct.setSize(product.getSize());
        if(product.getSellingPrice() != null || product.getMrpPrice() != null) {
            int discount = calculateDiscountPercentage(
                    existingProduct.getMrpPrice() != null ? existingProduct.getMrpPrice().intValue() : 0,
                    existingProduct.getSellingPrice() != null ? existingProduct.getSellingPrice().intValue() : 0
            );
            existingProduct.setDiscountPercent(discount);
        }
        return productRepository.save(existingProduct);
    }

    @Override
    public Product findProductById(Long productId)throws ProductException {
        return productRepository.findById(productId).orElseThrow(()->new ProductException("product not found with product id : "+productId));
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.searchProduct(query);
    }

    @Override
    public Page<Product> getAllProducts(String category,String brand, String color, String size, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber) {
        Specification<Product> specification=(root, query, criteriaBuilder)->{
            List<Predicate> predicates=new ArrayList<>();

            if(category!=null){
                Join<Product, Category> categoryJoin=root.join("category");
                Join<Category, Category> parent2Join=categoryJoin.join("parentCategory", jakarta.persistence.criteria.JoinType.LEFT);
                Join<Category, Category> parent1Join=parent2Join.join("parentCategory", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(categoryJoin.get("categoryId"), category),
                        criteriaBuilder.equal(parent2Join.get("categoryId"), category),
                        criteriaBuilder.equal(parent1Join.get("categoryId"), category)
                ));
            }
            if(brand!=null&& !brand.isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("brand"),brand));
            }
            if(color!=null&& !color.isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("color"),color));
            }
            if(size!=null&& !size.isEmpty()){
                predicates.add(criteriaBuilder.equal(root.get("size"),size));
            }

            if(minPrice!=null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"),BigDecimal.valueOf(minPrice)));
            }
            if(maxPrice!=null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"),BigDecimal.valueOf(maxPrice)));
            }
            if(minDiscount!=null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercent"),(double) minDiscount));
            }
            if(stock != null) {
                if (stock.equals("in_stock"))
                    predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), 0));
                else if (stock.equals("out_of_stock"))
                    predicates.add(criteriaBuilder.equal(root.get("quantity"), 0));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable;
        if(sort!=null && !sort.isEmpty()){
            switch(sort){
                case "price_low":
                    pageable= PageRequest.of(pageNumber!=null?pageNumber:0, 10, Sort.by("sellingPrice").ascending());
                    break;
                case "price_high":
                    pageable= PageRequest.of(pageNumber!=null?pageNumber:0, 10, Sort.by("sellingPrice").descending());
                    break;
                default:
                    pageable= PageRequest.of(pageNumber!=null?pageNumber:0, 10, Sort.unsorted());
                    break;
            }
        }
        else{
            pageable=PageRequest.of(pageNumber!=null?pageNumber:0,10,Sort.unsorted());
        }
        return productRepository.findAll(specification, pageable);
    }

    @Override
    public List<Product> getProductBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}
