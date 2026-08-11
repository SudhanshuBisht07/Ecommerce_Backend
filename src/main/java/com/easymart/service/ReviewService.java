package com.easymart.service;

import com.easymart.model.Product;
import com.easymart.model.Review;
import com.easymart.model.User;
import com.easymart.request.CreateReviewRequest;

import java.util.List;

public interface ReviewService {
    Review createReview(CreateReviewRequest request, User user, Product product) throws Exception;
    List<Review> getReviewByProductId(Long productId);
    Review updateReview(Long reviewId, String reviewText, double rating, Long userId) throws Exception;
    void deleteReview(Long reviewId, Long userId) throws Exception;
    Review getReviewById(Long reviewId) throws Exception;
    boolean hasPurchased(Long userId, Long productId);
    boolean hasReviewed(Long userId, Long productId);
}
