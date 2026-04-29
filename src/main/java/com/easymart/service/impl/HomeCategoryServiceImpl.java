package com.easymart.service.impl;

import com.easymart.model.HomeCategory;
import com.easymart.repository.HomeCategoryRepository;
import com.easymart.service.HomeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

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

        return homeCategoryRepository.save(existingCategory);
    }

    @Override
    public List<HomeCategory> getAllHomeCategories() {
        return homeCategoryRepository.findAll();
    }
}
