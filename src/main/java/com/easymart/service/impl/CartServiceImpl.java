package com.easymart.service.impl;

import com.easymart.model.Cart;
import com.easymart.model.CartItem;
import com.easymart.model.Product;
import com.easymart.model.User;
import com.easymart.repository.CartItemRepository;
import com.easymart.repository.CartRepository;
import com.easymart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    public CartItem addCartItem(User user, Product product, String size, int quantity) {
        Cart cart=findUserCart(user);
        CartItem isPresent=cartItemRepository.findByCartAndProductAndSize(cart, product, size);
        if(isPresent==null){
            CartItem cartItem=new CartItem();
            if (product.getSellingPrice() == null) {
                throw new IllegalArgumentException("Product selling price is not set for product id: " + product.getId());
            }
            if (product.getMrpPrice() == null) {
                throw new IllegalArgumentException("Product MRP price is not set for product id: " + product.getId());
            }
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUserId(user.getId());
            cartItem.setSize(size);
            BigDecimal totalPrice = product.getSellingPrice().multiply(BigDecimal.valueOf(quantity));
            cartItem.setSellingPrice(totalPrice);
            cartItem.setMrpPrice(product.getMrpPrice().multiply(BigDecimal.valueOf(quantity)));
            cartItem.setCart(cart);
            return cartItemRepository.save(cartItem);
        }
        return isPresent;
    }

    @Override
    public Cart findUserCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalDiscountedPrice = BigDecimal.ZERO;
        int totalItem = 0;
        for (CartItem cartItem : cart.getCartItems()) {
            totalPrice = totalPrice.add(cartItem.getMrpPrice());
            totalDiscountedPrice = totalDiscountedPrice.add(cartItem.getSellingPrice());
            totalItem += cartItem.getQuantity();
        }
        cart.setTotalMrpPrice(totalPrice);
        cart.setTotalItems(totalItem);
        cart.setTotalSellingPrice(totalDiscountedPrice);
        cart.setDiscount(totalPrice.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : calculateDiscountPercentage(totalPrice, totalDiscountedPrice));
        return cart;
    }

    private BigDecimal calculateDiscountPercentage(BigDecimal mrpPrice, BigDecimal sellingPrice) {
        if (mrpPrice.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("actual price must be greater than zero");
        }
        if (mrpPrice.compareTo(sellingPrice) < 0) {
            throw new IllegalArgumentException("mrp price must be greater than selling price");
        }
        double mrp = mrpPrice.doubleValue();
        double selling = sellingPrice.doubleValue();
        double discount = ((mrp - selling) / mrp) * 100;
        return BigDecimal.valueOf(discount);
    }
}

