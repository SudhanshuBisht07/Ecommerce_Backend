package com.easymart.service;

import com.easymart.model.Cart;
import com.easymart.model.Coupon;
import com.easymart.model.User;

import java.util.List;

public interface CouponService {
    Cart applyCoupon(String code, double orderValue, User user) throws Exception;
    Cart removeCoupon(String code, User user) throws Exception;
    Coupon findCouponById(Long id) throws Exception;
    Coupon createCoupon(Coupon coupon);
    List<Coupon> findAllCoupons();
    void deleteCoupon(Long id) throws Exception;
    Coupon updateCouponStatus(Long id, boolean isActive) throws Exception;

    // Active, currently-in-date coupons a shopper can actually use right now.
    // user is optional (null when the caller isn't authenticated) — when
    // present, coupons they've already used are excluded.
    List<Coupon> getAvailableCoupons(User user);

}
