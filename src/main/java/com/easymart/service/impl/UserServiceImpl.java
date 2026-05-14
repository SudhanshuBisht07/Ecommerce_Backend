package com.easymart.service.impl;

import com.easymart.service.UserService;
import com.easymart.config.JwtProvider;
import com.easymart.model.User;
import com.easymart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    @Transactional
    @Override
    public User findUserByJwtToken(String jwt) throws Exception {
        String email = jwtProvider.getEmailFromJwtToken(jwt);
        if (email != null && email.startsWith("seller_")) {
            throw new Exception("Seller token cannot be used on user endpoints");
        }
        return this.findUserByEmail(email);
    }
    @Transactional
    @Override
    public User findUserByEmail(String email) throws Exception {
        User user=userRepository.findByEmail(email);
        if(user==null){
            throw new Exception("user not found with email- "+email);
        }
        return user;
    }
}
