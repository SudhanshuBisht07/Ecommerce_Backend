package com.easymart.controller;

import com.easymart.model.Product;
import com.easymart.model.User;
import com.easymart.model.Wishlist;
import com.easymart.service.ProductService;
import com.easymart.service.UserService;
import com.easymart.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping()
    public ResponseEntity<Wishlist> getWishListByUserId(
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user=userService.findUserByJwtToken(jwt);
        Wishlist wishlist=wishlistService.getWishlistByUserId(user);
        return ResponseEntity.ok(wishlist);
    }
    @PostMapping("/add-product/{productId}")
    public ResponseEntity<Wishlist> addProductToWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization")String jwt)throws Exception{

        Product product=productService.findProductById(productId);
        User user=userService.findUserByJwtToken(jwt);
        Wishlist updateWishlist=wishlistService.addProductToWishlist(user, product);
        return ResponseEntity.ok(updateWishlist);
    }
}
