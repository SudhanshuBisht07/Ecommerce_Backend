package com.easymart.service;

import com.easymart.model.Category;
import com.easymart.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    Category createCategory(String name, String categoryId, Integer level, String parentCategoryId) throws Exception;

    Category updateCategory(Long id, String name) throws Exception;

    void deleteCategory(Long id) throws Exception;

}