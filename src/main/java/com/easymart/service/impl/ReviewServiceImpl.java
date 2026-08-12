package com.easymart.service.impl;

import com.easymart.domain.OrderStatus;
import com.easymart.model.Product;
import com.easymart.model.Review;
import com.easymart.model.User;
import com.easymart.repository.OrderItemRepository;
import com.easymart.repository.ProductRepository;
import com.easymart.repository.ReviewRepository;
import com.easymart.request.CreateReviewRequest;
import com.easymart.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
@Transactional
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    private static final List<OrderStatus> NON_PURCHASE_STATUSES =
            Arrays.asList(OrderStatus.PENDING, OrderStatus.CANCELLED);

    private void updateProductRating(Product product) {
        List<Review> allReviews = reviewRepository.findByProductId(product.getId());
        product.setNumRatings(allReviews.size());
        double avg = allReviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        product.setAvgRatings(avg);
        productRepository.save(product);
    }

    @Override
    public boolean hasPurchased(Long userId, Long productId) {
        // Reviewing requires the order to have actually been delivered,
        // not just placed/paid — a customer shouldn't be able to review a
        // product they haven't received yet.
        return orderItemRepository.existsByOrder_User_IdAndProduct_IdAndOrder_OrderStatus(
                userId, productId, OrderStatus.DELIVERED);
    }

    @Override
    public boolean hasReviewed(Long userId, Long productId) {
        return reviewRepository.existsByUser_IdAndProduct_Id(userId, productId);
    }

    @Override
    public Review createReview(CreateReviewRequest request, User user, Product product) throws Exception {
        if (!hasPurchased(user.getId(), product.getId())) {
            throw new Exception("You can only review a product after your order for it has been delivered");
        }
        if (hasReviewed(user.getId(), product.getId())) {
            throw new Exception("You've already reviewed this product — edit your existing review instead");
        }

        Review review=new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setReviewText(request.getReviewText());
        review.setRating(request.getReviewRating());
        review.setProductImages(request.getProductImages());

        product.getReviews().add(review);
        Review savedReview = reviewRepository.save(review);

        updateProductRating(product);
        return savedReview;
    }

    @Override
    public List<Review> getReviewByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public Review updateReview(Long reviewId, String reviewText, double rating, Long userId) throws Exception {
        Review review = getReviewById(reviewId);
        if (review.getUser().getId().equals(userId)) {
            review.setReviewText(reviewText);
            review.setRating(rating);
            Review saved = reviewRepository.save(review);
            Product freshProduct = productRepository.findById(review.getProduct().getId())
                    .orElseThrow(() -> new Exception("Product not found"));
            updateProductRating(freshProduct);
            return saved;
        }
        throw new Exception("you cant update this review");
    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId, Long userId) throws Exception {
        Review review = getReviewById(reviewId);
        if (!review.getUser().getId().equals(userId))
            throw new Exception("you cant delete this review");
        Product product = review.getProduct();
        product.getReviews().remove(review);
        productRepository.save(product);
        updateProductRating(product);
    }

    @Override
    public Review getReviewById(Long reviewId) throws Exception {
        return reviewRepository.findById(reviewId).orElseThrow(()->new Exception("review not found"));
    }
}
