package com.easymart.service.impl;

import com.easymart.service.ProductService;
import com.easymart.exceptions.ProductException;
import com.easymart.model.Category;
import com.easymart.model.Product;
import com.easymart.model.Seller;
import com.easymart.repository.CartItemRepository;
import com.easymart.repository.CategoryRepository;
import com.easymart.repository.OrderItemRepository;
import com.easymart.repository.ProductRepository;
import com.easymart.repository.WishlistRepository;
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
    private final CartItemRepository cartItemRepository;
    private final WishlistRepository wishlistRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Product createProduct(CreateProductRequest req, Seller seller) {
        Category category1=categoryRepository.findByCategoryId(req.getCategory());
        if(category1==null){
            Category category=new Category();
            category.setCategoryId(req.getCategory());
            category.setName(req.getCategory());
            category.setLevel(1);
            category1=categoryRepository.save(category);
        }
        Category leafCategory = category1;

        if(req.getCategory2()!=null && !req.getCategory2().isBlank()){
            Category category2=categoryRepository.findByCategoryId(req.getCategory2());
            if(category2==null){
                Category category=new Category();
                category.setCategoryId(req.getCategory2());
                category.setName(req.getCategory2());
                category.setLevel(2);
                category.setParentCategory(category1);
                category2=categoryRepository.save(category);
            }
            leafCategory = category2;

            if(req.getCategory3()!=null && !req.getCategory3().isBlank()){
                Category category3=categoryRepository.findByCategoryId(req.getCategory3());
                if(category3==null){
                    Category category=new Category();
                    category.setCategoryId(req.getCategory3());
                    category.setName(req.getCategory3());
                    category.setLevel(3);
                    category.setParentCategory(category2);
                    category3=categoryRepository.save(category);
                }
                leafCategory = category3;
            }
        }
        Product product= new Product();
        product.setSeller(seller);
        product.setCategory(leafCategory);
        product.setBrand(req.getBrand());
        product.setDescription(req.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setImages(req.getImages());
        product.setSize(req.getSize());
        product.setSellingPrice(req.getSellingPrice());
        product.setMrpPrice(req.getMrpPrice());
        product.setWholesalePrice(req.getWholesalePrice());
        product.setQuantity(req.getQuantity());
        int discountPercentage=calculateDiscountPercentage(req.getMrpPrice(), req.getSellingPrice());
        product.setDiscountPercent(discountPercentage);
        return productRepository.save(product);
    }
    private int calculateDiscountPercentage(BigDecimal mrpPrice, BigDecimal sellingPrice){
        if(mrpPrice.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("actual price must be greater than zero");
        }
        BigDecimal discount = mrpPrice.subtract(sellingPrice);
        if(discount.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("actual price must be greater than selling price");
        }
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(mrpPrice, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteProduct(Long productId)throws ProductException {
        Product product=findProductById(productId);

        if (orderItemRepository.existsByProduct(product)) {
            throw new ProductException("Cannot delete a product that has existing orders. Set its stock to 0 instead.");
        }

        cartItemRepository.deleteAll(cartItemRepository.findByProduct(product));

        for (var wishlist : wishlistRepository.findByProductsContaining(product)) {
            wishlist.getProducts().remove(product);
            wishlistRepository.save(wishlist);
        }

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
        if(product.getWholesalePrice() != null)
            existingProduct.setWholesalePrice(product.getWholesalePrice());
        if(product.getColor() != null)
            existingProduct.setColor(product.getColor());
        if(product.getImages() != null)
            existingProduct.setImages(product.getImages());
        if(product.getSize() != null)
            existingProduct.setSize(product.getSize());
        // quantity is a primitive on Product, so there's no null-check to gate
        // it the way the other fields do — the PUT contract for this endpoint
        // is that the caller sends the full desired quantity, not a partial patch.
        existingProduct.setQuantity(product.getQuantity());
        if(product.getCategory() != null && product.getCategory().getCategoryId() != null){
            Category category = categoryRepository.findByCategoryId(product.getCategory().getCategoryId());
            if(category != null){
                existingProduct.setCategory(category);
            }
        }
        if (existingProduct.getMrpPrice() != null && existingProduct.getSellingPrice() != null) {
            if (existingProduct.getSellingPrice().compareTo(existingProduct.getMrpPrice()) > 0) {
                throw new ProductException("Selling price cannot be greater than MRP price");
            }
            int discount = calculateDiscountPercentage(
                    existingProduct.getMrpPrice(),
                    existingProduct.getSellingPrice()
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
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercent"), minDiscount));
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
        return productRepository.findBySeller_Id(sellerId);
    }
}
