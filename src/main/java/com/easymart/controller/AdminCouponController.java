package com.easymart.controller;

import com.easymart.model.Cart;
import com.easymart.model.Coupon;
import com.easymart.model.User;
import com.easymart.service.CartService;
import com.easymart.service.CouponService;
import com.easymart.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class AdminCouponController {
    private final CouponService couponService;
    private final UserService userService;
    private final CartService cartService;

    @PostMapping("/apply")
    public ResponseEntity<Cart> applyCoupon(
            @RequestParam boolean apply,
            @RequestParam String code,
            @RequestParam double orderValue,
            @RequestHeader("Authorization") String jwt)throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        Cart cart;
        if(apply){
            cart=couponService.applyCoupon(code, orderValue, user);
        }
        else{
            cart=couponService.removeCoupon(code, user);
        }
        return ResponseEntity.ok(cart);
    }
    @PostMapping("/admin/create")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon){
        Coupon createdCoupon=couponService.createCoupon(coupon);
        return ResponseEntity.ok(createdCoupon);
    }
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) throws Exception {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok("coupon deleted successfully");
    }
    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<Coupon> updateCouponStatus(@PathVariable Long id, @RequestParam boolean isActive) throws Exception {
        Coupon coupon = couponService.updateCouponStatus(id, isActive);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Coupon>> getAllCoupons(){
        List<Coupon> coupons=couponService.findAllCoupons();
        return ResponseEntity.ok(coupons);
    }
}
