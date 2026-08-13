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

    // Resolves a seller's L1/L2/L3 category picks into the leaf Category
    // node, creating any node that doesn't exist yet — this is what lets a
    // seller define their own custom leaf category instead of being locked
    // to the existing taxonomy. Shared by create and update so a custom
    // category picked while editing a product is created the same way a
    // custom category picked while adding one is.
    private Category resolveLeafCategory(String category, String category2, String category3) {
        Category category1 = categoryRepository.findByCategoryId(category);
        if (category1 == null) {
            Category c = new Category();
            c.setCategoryId(category);
            c.setName(category);
            c.setLevel(1);
            category1 = categoryRepository.save(c);
        }
        Category leafCategory = category1;

        if (category2 != null && !category2.isBlank()) {
            Category cat2 = categoryRepository.findByCategoryId(category2);
            if (cat2 == null) {
                Category c = new Category();
                c.setCategoryId(category2);
                c.setName(category2);
                c.setLevel(2);
                c.setParentCategory(category1);
                cat2 = categoryRepository.save(c);
            }
            leafCategory = cat2;

            if (category3 != null && !category3.isBlank()) {
                Category cat3 = categoryRepository.findByCategoryId(category3);
                if (cat3 == null) {
                    Category c = new Category();
                    c.setCategoryId(category3);
                    c.setName(category3);
                    c.setLevel(3);
                    c.setParentCategory(cat2);
                    cat3 = categoryRepository.save(c);
                }
                leafCategory = cat3;
            }
        }
        return leafCategory;
    }

    @Override
    public Product createProduct(CreateProductRequest req, Seller seller) {
        Category leafCategory = resolveLeafCategory(req.getCategory(), req.getCategory2(), req.getCategory3());
        Product product= new Product();
        product.setSeller(seller);
        product.setCategory(leafCategory);
        product.setBrand(req.getBrand());
        product.setDescription(req.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(req.getTitle());
        product.setColor(req.getColor());
        product.setImages(req.getImages());
        product.setSizes(req.getSizes() != null ? req.getSizes() : new java.util.ArrayList<>());
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
    public Product updateProduct(Long productId, CreateProductRequest product) throws ProductException {
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
        if(product.getSizes() != null)
            existingProduct.setSizes(product.getSizes());
        // quantity is a primitive, so there's no null-check to gate it the
        // way the other fields do — the PUT contract for this endpoint is
        // that the caller sends the full desired quantity, not a partial patch.
        existingProduct.setQuantity(product.getQuantity());
        if(product.getCategory() != null && !product.getCategory().isBlank()){
            // Resolves (and creates, if needed) the leaf the same way
            // createProduct does — so picking a brand-new custom category
            // while editing works exactly like it does while adding.
            Category leafCategory = resolveLeafCategory(product.getCategory(), product.getCategory2(), product.getCategory3());
            existingProduct.setCategory(leafCategory);
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
                Join<Product, String> sizeJoin = root.join("sizes", jakarta.persistence.criteria.JoinType.INNER);
                predicates.add(criteriaBuilder.equal(sizeJoin, size));
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

    @Override
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueOrderByCreatedAtDesc();
    }

    @Override
    public Product setFeatured(Long productId, boolean featured) throws ProductException {
        Product product = findProductById(productId);
        product.setFeatured(featured);
        return productRepository.save(product);
    }
}
