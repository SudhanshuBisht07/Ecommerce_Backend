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

    @Override
    public Category createCategory(String name, String categoryId, Integer level, String parentCategoryId) throws Exception {
        if (categoryRepository.findByCategoryId(categoryId) != null) {
            throw new Exception("A category with id '" + categoryId + "' already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setCategoryId(categoryId);
        category.setLevel(level);

        if (parentCategoryId != null && !parentCategoryId.isBlank()) {
            Category parent = categoryRepository.findByCategoryId(parentCategoryId);
            if (parent == null) {
                throw new Exception("Parent category not found");
            }
            category.setParentCategory(parent);
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, String name) throws Exception {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Category not found"));
        category.setName(name);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Category not found"));
        categoryRepository.delete(category);
    }
}