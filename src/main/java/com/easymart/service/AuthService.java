package com.easymart.service;

import com.easymart.domain.USER_ROLE;
import com.easymart.request.LoginRequest;
import com.easymart.response.AuthResponse;
import com.easymart.request.SignupRequest;

public interface AuthService {

    void sentLoginOtp(String email, USER_ROLE role) throws Exception;
    String createUser(SignupRequest req) throws Exception;
    AuthResponse signing(LoginRequest req);




}
