package com.easymart.request;

import com.easymart.domain.USER_ROLE;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginOtpRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String otp;

    @NotNull(message = "Role is required")
    private USER_ROLE role;

    private boolean isLogin;
}
