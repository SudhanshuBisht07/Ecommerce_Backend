package com.easymart.request;

import com.easymart.domain.ReturnStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private ReturnStatus status;

    private String sellerNote;
}
