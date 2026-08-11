package com.easymart.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCategoryRequest {

    private String name;

    private String categoryId;

    private Integer level;

    // categoryId of the parent taxonomy node; optional (L1 categories have none).
    private String parentCategoryId;
}
