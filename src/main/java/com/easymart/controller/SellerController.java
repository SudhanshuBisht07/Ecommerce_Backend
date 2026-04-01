package com.easymart.controller;

import com.easymart.Service.AuthService;
import com.easymart.Service.EmailService;
import com.easymart.Service.SellerService;
import com.easymart.config.JwtProvider;
import com.easymart.domain.AccountStatus;
import com.easymart.model.Seller;
import com.easymart.model.SellerReport;
import com.easymart.model.VerificationCode;
import com.easymart.repository.VerificationCodeRepository;
import com.easymart.request.LoginRequest;
import com.easymart.response.ApiResponse;
import com.easymart.response.AuthResponse;
import com.easymart.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {
    private final AuthService authService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final SellerService sellerService;
    private final EmailService emailService;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginSeller(@RequestBody LoginRequest req) throws Exception {
        String otp = req.getOtp();
        String email = req.getEmail();
        req.setEmail("seller_" + email);
        AuthResponse authResponse = authService.signing(req);
        return ResponseEntity.ok(authResponse);

    }
    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(@PathVariable String otp)throws Exception{
        VerificationCode verificationCode=verificationCodeRepository.findByOtp(otp);
        if(verificationCode==null|| !verificationCode.getOtp().equals(otp)){
            throw new Exception("wrong otp");
        }
        Seller seller=sellerService.verifyEmail(verificationCode.getEmail(), otp);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }
    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller) throws Exception{
        Seller savedSeller=sellerService.createSeller(seller);
        String otp= OtpUtil.generateOtp();
        VerificationCode verificationCode=new VerificationCode();
        verificationCode.setOtp(otp);
        verificationCode.setEmail(savedSeller.getEmail());
        verificationCodeRepository.save(verificationCode);
        String subject="EasyMart email verification code.";
        String text="Welcome to EasyMart, verify your email using this link.";
        String frontend_url="http://localhost:3000/verify-seller/";
        emailService.sendVerificationOtpEmail(savedSeller.getEmail(), verificationCode.getOtp(), subject, text + frontend_url);
        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id)throws Exception {
        Seller seller = sellerService.getSellerById(id);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }
    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByJwt(@RequestHeader("Authorization")String jwt)throws Exception{
        Seller seller=sellerService.getSellerProfile(jwt);
        return new ResponseEntity<>(seller, HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(@RequestParam(required = false)AccountStatus status){
        List<Seller> sellers= sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellers);
    }
    @PatchMapping()
    public ResponseEntity<Seller> updateSeller(@RequestHeader("Authorization")String jwt, @RequestBody Seller seller)throws Exception{
        Seller profile=sellerService.getSellerProfile(jwt);
        Seller updatedSeller=sellerService.updateSeller(profile.getId(), seller);
        return ResponseEntity.ok(updatedSeller);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id)throws Exception{
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }
//    @GetMapping("/report")
//    public ResponseEntity<SellerReport> getSellerReport(@RequestHeader("Authorization")String jwt)throws Exception{
//        String email=jwtProvider.getEmailFromJwtToken(jwt);
//        Seller seller=sellerService.getSellerByEmail(email);
//        SellerReport report=sellerReportService.getSellerReport(seller);
//        return new ResponseEntity<>(report, HttpStatus.OK);
//    }

}