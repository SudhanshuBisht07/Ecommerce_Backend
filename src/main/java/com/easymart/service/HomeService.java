package com.easymart.service;

import com.easymart.model.Home;
import com.easymart.model.HomeCategory;

import java.util.List;

public interface HomeService {
    public Home createHomePageData(List<HomeCategory> allCategories);
}
