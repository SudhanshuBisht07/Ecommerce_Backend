package com.easymart.controller;

import com.easymart.service.AuthService;
import com.easymart.domain.USER_ROLE;
import com.easymart.request.LoginOtpRequest;
import com.easymart.request.LoginRequest;
import com.easymart.response.ApiResponse;
import com.easymart.response.AuthResponse;
import com.easymart.request.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> createUserHandler(@Valid @RequestBody SignupRequest req) throws Exception {
        String jwt=authService.createUser(req);
        AuthResponse res=new AuthResponse();
        res.setJwt(jwt);
        res.setMessage("register success");
        res.setRole(USER_ROLE.ROLE_CUSTOMER);
        return ResponseEntity.ok(res);
    }
    @PostMapping("/sent/login-signup-otp")
    public ResponseEntity<ApiResponse> setOtpHandler(@Valid @RequestBody LoginOtpRequest req) throws Exception {
        authService.sentLoginOtp(req.getEmail(), req.getRole(), req.isLogin());
        ApiResponse res=new ApiResponse();
        res.setMessage("otp sent successfully");
        return ResponseEntity.ok(res);
    }
    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> loginHandler(@Valid @RequestBody LoginRequest req) throws Exception {
        AuthResponse authResponse=authService.signing(req);
        return ResponseEntity.ok(authResponse);
    }
}
