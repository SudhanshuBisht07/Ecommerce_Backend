package com.easymart.service.impl;

import com.easymart.model.Category;
import com.easymart.repository.CategoryRepository;
import com.easymart.response.CategoryResponse;
import com.easymart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {

        List<Category> categories =
                categoryRepository.findAllByOrderByLevelAscNameAsc();

        return categories
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getCategoryId(),
                        category.getLevel()
                ))
                .toList();
    }
}