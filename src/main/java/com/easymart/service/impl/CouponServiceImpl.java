package com.easymart.service.impl;

import com.easymart.model.Cart;
import com.easymart.model.Coupon;
import com.easymart.model.User;
import com.easymart.repository.CartRepository;
import com.easymart.repository.CouponRepository;
import com.easymart.repository.UserRepository;
import com.easymart.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor

public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;


    @Override
    public Cart applyCoupon(String code, double orderValue, User user) throws Exception {
        Coupon coupon=couponRepository.findByCode(code);
        Cart cart=cartRepository.findByUserId(user.getId());
        if(coupon==null)
            throw new Exception("coupon not valid");

        if(user.getUsedCoupons().contains(coupon))
            throw new Exception("coupon already used");

        if(BigDecimal.valueOf(orderValue).compareTo(coupon.getMinimumOrderValue()) < 0)
            throw new Exception("order value is less than minimum order value: "+coupon.getMinimumOrderValue());

        if(coupon.isActive()&& LocalDate.now().isAfter(coupon.getValidityStartDate())&& LocalDate.now().isBefore(coupon.getValidityEndDate())){
            user.getUsedCoupons().add(coupon);
            userRepository.save(user);

            BigDecimal discountedPrice = cart.getTotalSellingPrice()
                    .multiply(coupon.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);

            cart.setTotalSellingPrice(cart.getTotalSellingPrice().subtract(discountedPrice));
            cart.setDiscount(discountedPrice);
            cart.setCouponCode(code);
            cartRepository.save(cart);
            return cart;
        }
        throw new Exception("coupon not valid");
    }

    @Override
    public Cart removeCoupon(String code, User user) throws Exception {
        Coupon coupon=couponRepository.findByCode(code);
        if(coupon==null)
            throw new Exception("coupon not valid");
        Cart cart=cartRepository.findByUserId(user.getId());
        BigDecimal discountedPrice = cart.getTotalSellingPrice()
                .multiply(coupon.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100),2, RoundingMode.HALF_UP);
        cart.setTotalSellingPrice(cart.getTotalSellingPrice().add(discountedPrice));
        cart.setCouponCode(null);
        cart.setDiscount(null);
        return cartRepository.save(cart);
    }

    @Override
    public Coupon findCouponById(Long id) throws Exception {
        return couponRepository.findById(id).orElseThrow(()->new Exception("coupon not found with id: "+id));
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> findAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('Admin')")
    public void deleteCoupon(Long id) throws Exception {
        findCouponById(id);
        couponRepository.deleteById(id);
    }
}
