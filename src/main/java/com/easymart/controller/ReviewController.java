package com.easymart.controller;

import com.easymart.model.Product;
import com.easymart.model.Review;
import com.easymart.model.User;
import com.easymart.request.CreateReviewRequest;
import com.easymart.response.ApiResponse;
import com.easymart.response.ReviewEligibilityResponse;
import com.easymart.service.ProductService;
import com.easymart.service.ReviewService;
import com.easymart.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<Review>> getReviewByProductId(
            @PathVariable Long productId){
        List<Review> reviews=reviewService.getReviewByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    // Lets the frontend decide whether to show "Write a review" at all,
    // without exposing purchase history for other users' products.
    @GetMapping("/products/{productId}/reviews/eligibility")
    public ResponseEntity<ReviewEligibilityResponse> getReviewEligibility(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user=userService.findUserByJwtToken(jwt);
        boolean hasPurchased = reviewService.hasPurchased(user.getId(), productId);
        boolean hasReviewed = reviewService.hasReviewed(user.getId(), productId);
        return ResponseEntity.ok(new ReviewEligibilityResponse(hasPurchased, hasReviewed));
    }

    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<Review> writeReview(
            @Valid @RequestBody CreateReviewRequest request,
            @PathVariable Long productId,
            @RequestHeader("Authorization")String jwt)throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        Product product=productService.findProductById(productId);
        Review review=reviewService.createReview(request, user, product);
        return ResponseEntity.ok(review);
    }
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @Valid @RequestBody CreateReviewRequest request,
            @PathVariable Long reviewId,
            @RequestHeader("Authorization")String jwt)throws Exception{
        User user= userService.findUserByJwtToken(jwt);
        Review review=reviewService.updateReview(reviewId, request.getReviewText(), request.getReviewRating(), user.getId());
        return ResponseEntity.ok(review);
    }
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization")String jwt)throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        reviewService.deleteReview(reviewId, user.getId());
        ApiResponse response=new ApiResponse();
        response.setMessage("review deleted successfully");
        return ResponseEntity.ok(response);
    }

}
