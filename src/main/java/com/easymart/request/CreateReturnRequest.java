package com.easymart.request;

import com.easymart.domain.ReturnType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReturnRequest {
    @NotNull(message = "Return type is required")
    private ReturnType type;

    @NotBlank(message = "A reason is required")
    private String reason;
}
