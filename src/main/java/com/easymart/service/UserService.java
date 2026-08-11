package com.easymart.service;

import com.easymart.model.User;

import java.util.List;

public interface UserService {
    public User findUserByJwtToken(String jwt) throws Exception;
    public User findUserByEmail(String email) throws Exception;
    public List<User> getAllUsers();
    public User updateProfileImage(String jwt, String imageUrl) throws Exception;
}
