package com.easymart.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;

    private String name;

    private String categoryId;

    private Integer level;

    // categoryId of the parent taxonomy node (null for L1 categories). Lets
    // the frontend group categories into a L1 -> L2 -> L3 tree instead of a
    // flat list.
    private String parentCategoryId;

}