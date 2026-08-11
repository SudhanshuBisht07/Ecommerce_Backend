package com.easymart.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class CreateProductRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String brand;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than 0")
    private BigDecimal mrpPrice;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than 0")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", message = "Wholesale price cannot be negative")
    private BigDecimal wholesalePrice;

    private String color;
    private List<String> images;

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    @NotBlank(message = "Category is required")
    private String category;

    private String category2;
    private String category3;
    private String size;

}
