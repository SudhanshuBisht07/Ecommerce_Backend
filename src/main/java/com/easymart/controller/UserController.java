package com.easymart.controller;

import com.easymart.response.ImageUploadResponse;
import com.easymart.service.CloudinaryService;
import com.easymart.service.UserService;
import com.easymart.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public ResponseEntity<User> createUserHandler(@RequestHeader("Authorization")String jwt)throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsersHandler(){
        List<User> users=userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String jwt) throws Exception {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return new ResponseEntity<>("Only image files are allowed", HttpStatus.BAD_REQUEST);
        }

        try {
            String imageUrl = cloudinaryService.uploadImage(file, "users");
            userService.updateProfileImage(jwt, imageUrl);

            return new ResponseEntity<>(new ImageUploadResponse(imageUrl), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>("Could not upload image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
