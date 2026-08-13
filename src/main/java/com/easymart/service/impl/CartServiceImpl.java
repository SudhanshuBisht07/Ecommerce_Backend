package com.easymart.service.impl;

import com.easymart.model.Cart;
import com.easymart.model.CartItem;
import com.easymart.model.Coupon;
import com.easymart.model.Product;
import com.easymart.model.User;
import com.easymart.repository.CartItemRepository;
import com.easymart.repository.CartRepository;
import com.easymart.repository.CouponRepository;
import com.easymart.repository.UserRepository;
import com.easymart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Override
    public CartItem addCartItem(User user, Product product, String size, int quantity) {
        // A product with sizes defined has no single "default" variant —
        // the customer must pick one of them. Products with no sizes at all
        // (sizes list empty) are unaffected.
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            if (size == null || size.isBlank()) {
                throw new IllegalArgumentException("Please select a size for: " + product.getTitle());
            }
            if (!product.getSizes().contains(size)) {
                throw new IllegalArgumentException("Selected size is not available for: " + product.getTitle());
            }
        }

        Cart cart=findUserCart(user);
        CartItem isPresent=cartItemRepository.findByCartAndProductAndSize(cart, product, size);

        if(isPresent!=null){
            int newQty = isPresent.getQuantity() + quantity;
            if (product.getQuantity() < newQty) {
                throw new IllegalArgumentException("Only " + product.getQuantity()
                        + " units available for: " + product.getTitle());
            }
            isPresent.setQuantity(newQty);
            isPresent.setSellingPrice(product.getSellingPrice().multiply(BigDecimal.valueOf(newQty)));
            isPresent.setMrpPrice(product.getMrpPrice().multiply(BigDecimal.valueOf(newQty)));
            return cartItemRepository.save(isPresent);
        }
        CartItem cartItem=new CartItem();
        if (product.getSellingPrice() == null) {
            throw new IllegalArgumentException("Product selling price is not set for product id: " + product.getId());
        }
        if (product.getMrpPrice() == null) {
            throw new IllegalArgumentException("Product MRP price is not set for product id: " + product.getId());
        }
        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Only " + product.getQuantity()
                    + " units available for: " + product.getTitle());
        }
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setUserId(user.getId());
        cartItem.setSize(size);
        cartItem.setSellingPrice(product.getSellingPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItem.setMrpPrice(product.getMrpPrice().multiply(BigDecimal.valueOf(quantity)));
        cartItem.setCart(cart);
        return cartItemRepository.save(cartItem);
    }

    @Override
    public Cart findUserCart(User user) {
        Cart cart = cartRepository.findByUserId(user.getId());
        if(cart == null){
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalDiscountedPrice = BigDecimal.ZERO;
        int totalItem = 0;
        for (CartItem cartItem : cart.getCartItems()) {
            totalPrice = totalPrice.add(cartItem.getMrpPrice());
            totalDiscountedPrice = totalDiscountedPrice.add(cartItem.getSellingPrice());
            totalItem += cartItem.getQuantity();
        }
        BigDecimal oldMrpPrice = cart.getTotalMrpPrice() != null ? cart.getTotalMrpPrice() : BigDecimal.ZERO;
        BigDecimal oldSellingPrice = cart.getTotalSellingPrice() != null ? cart.getTotalSellingPrice() : BigDecimal.ZERO;
        int oldTotalItems = cart.getTotalItems();

        cart.setTotalMrpPrice(totalPrice);
        cart.setTotalItems(totalItem);

        BigDecimal calculatedDiscount = totalPrice.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : totalPrice.subtract(totalDiscountedPrice);

        // A coupon's discount was captured as a flat amount at the moment it
        // was applied. If the cart total has since changed (an item was
        // removed/its quantity dropped), that flat amount is stale — and if
        // the cart no longer meets the coupon's minimum order value, it
        // shouldn't still be "applied" at all. Re-validate every time the
        // cart is recomputed instead of blindly reusing what was stored.
        if (cart.getCouponCode() != null) {
            Coupon coupon = couponRepository.findByCode(cart.getCouponCode());
            boolean stillEligible = coupon != null
                    && totalDiscountedPrice.compareTo(coupon.getMinimumOrderValue()) >= 0;

            if (!stillEligible) {
                if (coupon != null && cart.getUser() != null) {
                    User cartOwner = cart.getUser();
                    if (cartOwner.getUsedCoupons().remove(coupon)) {
                        userRepository.save(cartOwner);
                    }
                }
                cart.setCouponCode(null);
                cart.setCouponDiscount(null);
                cart.setTotalSellingPrice(totalDiscountedPrice);
                cart.setDiscount(calculatedDiscount);
            } else {
                BigDecimal recalculatedCouponDiscount = totalDiscountedPrice
                        .multiply(coupon.getDiscountPercentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                cart.setCouponDiscount(recalculatedCouponDiscount);
                cart.setTotalSellingPrice(totalDiscountedPrice.subtract(recalculatedCouponDiscount));
                cart.setDiscount(calculatedDiscount.add(recalculatedCouponDiscount));
            }
        } else {
            cart.setTotalSellingPrice(totalDiscountedPrice);
            cart.setDiscount(calculatedDiscount);
        }
        boolean changed = !totalPrice.equals(oldMrpPrice)
                || !cart.getTotalSellingPrice().equals(oldSellingPrice)
                || totalItem != oldTotalItems;

        if (changed) {
            return cartRepository.save(cart);
        }
        return cart;
    }

}

