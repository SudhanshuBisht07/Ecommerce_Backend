package com.easymart.repository;

import com.easymart.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository <VerificationCode, Long> {
    VerificationCode findByEmail(String email);
    VerificationCode findByOtp(String otp);
    VerificationCode findByEmailAndPurpose(String email, String purpose);
}
