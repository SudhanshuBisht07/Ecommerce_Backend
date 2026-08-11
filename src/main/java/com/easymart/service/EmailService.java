package com.easymart.service;

public interface EmailService {
    void sendVerificationOtpEmail(
            String userEmail,
            String otp,
            String subject,
            String text) throws Exception;
}