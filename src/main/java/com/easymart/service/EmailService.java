package com.easymart.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendVerificationOtpEmail(
            String userEmail,
            String otp,
            String subject,
            String text) throws MessagingException;
}

