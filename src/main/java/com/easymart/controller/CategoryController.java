package com.easymart.controller;

import com.easymart.model.Category;
import com.easymart.request.CreateCategoryRequest;
import com.easymart.response.CategoryResponse;
import com.easymart.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    // Admin-only taxonomy management (L1/L2/L3 electronics/mobiles/smartphones
    // style categories). Previously there was no endpoint for this at all, so
    // the admin panel could only display these as read-only.
    @PostMapping("/admin")
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequest req) throws Exception {
        Category category = categoryService.createCategory(
                req.getName(), req.getCategoryId(), req.getLevel(), req.getParentCategoryId()
        );
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    @PatchMapping("/admin/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody CreateCategoryRequest req) throws Exception {
        Category category = categoryService.updateCategory(id, req.getName());
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) throws Exception {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}