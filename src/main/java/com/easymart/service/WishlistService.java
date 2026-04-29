package com.easymart.service;

import com.easymart.model.Product;
import com.easymart.model.User;
import com.easymart.model.Wishlist;

public interface WishlistService {
    Wishlist createWishlist(User user);
    Wishlist getWishlistByUserId(User user);
    Wishlist addProductToWishlist(User user, Product product);
}
