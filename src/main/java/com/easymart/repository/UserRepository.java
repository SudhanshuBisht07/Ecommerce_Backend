package com.easymart.repository;

import com.easymart.model.Coupon;
import com.easymart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    List<User> findByUsedCouponsContaining(Coupon coupon);
}
