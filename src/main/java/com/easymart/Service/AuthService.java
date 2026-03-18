package com.easymart.Service;

import com.easymart.model.User;
import com.easymart.request.LoginRequest;
import com.easymart.response.AuthResponse;
import com.easymart.response.SignupRequest;

public interface AuthService {

    void sentLoginOtp(String email) throws Exception;
    String createUser(SignupRequest req) throws Exception;
    AuthResponse signing(LoginRequest req);




}
