package com.easymart.service;

import com.easymart.model.Cart;
import com.easymart.model.CartItem;
import com.easymart.model.Product;
import com.easymart.model.User;

public interface CartService {
    public CartItem addCartItem(User user, Product product, String size, int quantity);
    public Cart findUserCart(User user);
}
