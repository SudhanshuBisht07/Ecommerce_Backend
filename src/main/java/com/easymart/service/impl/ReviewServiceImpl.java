package com.easymart.service.impl;

import com.easymart.model.Product;
import com.easymart.model.Review;
import com.easymart.model.User;
import com.easymart.repository.ReviewRepository;
import com.easymart.request.CreateReviewRequest;
import com.easymart.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EmptyStackException;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;


    @Override
    public Review createReview(CreateReviewRequest request, User user, Product product) {
        Review review=new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setReviewText(request.getReviewText());
        review.setRating(request.getReviewRating());
        review.setProductImages(request.getProductImages());

        product.getReviews().add(review);
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getReviewByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public Review updateReview(Long reviewId, String reviewText, double rating, Long userId) throws Exception {
        Review review=getReviewById(reviewId);
        if(review.getUser().getId().equals(userId)) {
            review.setReviewText(reviewText);
            review.setRating(rating);
            return reviewRepository.save(review);
        }
        throw new Exception("you cant update this review");
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) throws Exception {
        Review review=getReviewById(reviewId);
        if(!review.getUser().getId().equals(userId))
            throw new Exception("you cant delete this review");
        reviewRepository.delete(review);
    }

    @Override
    public Review getReviewById(Long reviewId) throws Exception {
        return reviewRepository.findById(reviewId).orElseThrow(()->new Exception("review not found"));
    }
}
