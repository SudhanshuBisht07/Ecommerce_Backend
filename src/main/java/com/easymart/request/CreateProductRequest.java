package com.easymart.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class CreateProductRequest {
    private String title;
    private String description;
    private String brand;
    private BigDecimal mrpPrice;
    private BigDecimal sellingPrice;
    private String color;
    private List<String> images;
    private int quantity;
    private String category;
    private String category2;
    private String category3;
    private String size;

}
