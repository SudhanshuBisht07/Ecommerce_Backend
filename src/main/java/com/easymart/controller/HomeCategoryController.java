package com.easymart.controller;

import com.easymart.model.Home;
import com.easymart.model.HomeCategory;
import com.easymart.service.HomeCategoryService;
import com.easymart.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomeCategoryController {
    private final HomeCategoryService homeCategoryService;
    private final HomeService homeService;

    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategories(@RequestBody List<HomeCategory> homeCategories){
        List<HomeCategory> categories=homeCategoryService.createCategories(homeCategories);
        Home home= homeService.createHomePageData(categories);
        return new ResponseEntity<>(home, HttpStatus.ACCEPTED);
    }

    // Powers the storefront homepage: groups every admin-configured tile by
    // its "Homepage section" (Shop by categories / Electric categories /
    // Grid / Deals) so the tiles created in the admin panel actually render
    // somewhere for shoppers instead of only existing in the admin table.
    @GetMapping("/home/categories")
    public ResponseEntity<Home> getHomePageData(){
        List<HomeCategory> categories=homeCategoryService.getAllHomeCategories();
        Home home= homeService.createHomePageData(categories);
        return ResponseEntity.ok(home);
    }

    @GetMapping("/admin/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory(){
        List<HomeCategory> categories=homeCategoryService.getAllHomeCategories();
        return ResponseEntity.ok(categories);
    }
    @PatchMapping("/admin/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(
            @PathVariable Long id,
            @RequestBody HomeCategory homeCategory)throws Exception{
        HomeCategory updatedCategory=homeCategoryService.updateHomeCategory(homeCategory, id);
        return ResponseEntity.ok(updatedCategory);
    }

}
