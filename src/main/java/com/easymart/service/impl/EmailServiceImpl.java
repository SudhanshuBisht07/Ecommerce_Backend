//package com.easymart.service.impl;
//
//import com.easymart.service.EmailService;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.MailException;
//import org.springframework.mail.MailSendException;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class EmailServiceImpl implements EmailService {
//    private final JavaMailSender javaMailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromAddress;
//
//    public void sendVerificationOtpEmail(String userEmail, String otp, String subject, String text) throws MessagingException {
//        try {
//            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
//            mimeMessageHelper.setFrom(fromAddress);
//            mimeMessageHelper.setSubject(subject);
//            mimeMessageHelper.setText(text);
//            mimeMessageHelper.setTo(userEmail);
//            javaMailSender.send(mimeMessage);
//        } catch (MailException e) {
//            throw new MailSendException("failed to send email", e);
//        }
//    }
//}
package com.easymart.service.impl;

import com.easymart.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromAddress;

    @Value("${brevo.from.name:EasyMart}")
    private String fromName;

    public EmailServiceImpl() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void sendVerificationOtpEmail(String userEmail, String otp, String subject, String text) throws Exception {
        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", fromName, "email", fromAddress),
                "to", List.of(Map.of("email", userEmail)),
                "subject", subject,
                "textContent", text
        );

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_API_URL))
                .header("api-key", brevoApiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            logger.error("Failed to reach Brevo API while sending OTP email to {}: {}", userEmail, e.getMessage(), e);
            throw new Exception("failed to send email", e);
        }

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.info("OTP email sent to {} via Brevo (status {})", userEmail, response.statusCode());
        } else {
            logger.error("Brevo API returned error status {} for {}: {}", response.statusCode(), userEmail, response.body());
            throw new Exception("failed to send email: " + response.body());
        }
    }
}