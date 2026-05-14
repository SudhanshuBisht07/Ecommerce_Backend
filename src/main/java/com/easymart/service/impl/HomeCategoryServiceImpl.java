package com.easymart.service.impl;

import com.easymart.model.HomeCategory;
import com.easymart.repository.HomeCategoryRepository;
import com.easymart.service.HomeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeCategoryServiceImpl implements HomeCategoryService {

    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    public HomeCategory createHomeCategory(HomeCategory homeCategory) {
        return homeCategoryRepository.save(homeCategory);
    }

    @Override
    public List<HomeCategory> createCategories(List<HomeCategory> homeCategories) {

        if(homeCategoryRepository.findAll().isEmpty())
            return homeCategoryRepository.saveAll(homeCategories);
        return homeCategoryRepository.findAll();
    }

    @Override
    public HomeCategory updateHomeCategory(HomeCategory homeCategory, Long id) throws Exception {
        HomeCategory existingCategory=homeCategoryRepository.findById(id)
                .orElseThrow(()->new Exception("category not found"));
        if(homeCategory.getImage()!=null)
            existingCategory.setImage(homeCategory.getImage());
        if(homeCategory.getCategoryId()!=null)
            existingCategory.setCategoryId(homeCategory.getCategoryId());
        if(homeCategory.getName() != null)
            existingCategory.setName(homeCategory.getName());
        if(homeCategory.getSection() != null)
            existingCategory.setSection(homeCategory.getSection());
        if(homeCategory.getDiscountPercentage() != null)
            existingCategory.setDiscountPercentage(homeCategory.getDiscountPercentage());

        return homeCategoryRepository.save(existingCategory);
    }

    @Override
    public List<HomeCategory> getAllHomeCategories() {
        return homeCategoryRepository.findAll();
    }
}
