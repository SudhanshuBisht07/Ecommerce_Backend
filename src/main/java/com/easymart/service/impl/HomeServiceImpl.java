package com.easymart.service.impl;

import com.easymart.domain.HomeCategorySection;
import com.easymart.model.Deal;
import com.easymart.model.Home;
import com.easymart.model.HomeCategory;
import com.easymart.repository.DealRepository;
import com.easymart.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private final DealRepository dealRepository;

    @Override
    public Home createHomePageData(List<HomeCategory> allCategories) {
        List<HomeCategory> gridCategories=allCategories.stream()
                .filter(homeCategory ->
                        homeCategory.getSection()== HomeCategorySection.GRID)
                .collect(Collectors.toList());
        List<HomeCategory> shopByCategories=allCategories.stream()
                .filter(homeCategory ->
                        homeCategory.getSection()== HomeCategorySection.SHOP_BY_CATEGORIES)
                .collect(Collectors.toList());
        List<HomeCategory> electricCategories=allCategories.stream()
                .filter(homeCategory ->
                        homeCategory.getSection()== HomeCategorySection.ELECTRIC_CATEGORIES)
                .collect(Collectors.toList());
        List<HomeCategory> dealCategories=allCategories.stream()
                .filter(homeCategory ->
                        homeCategory.getSection()== HomeCategorySection.DEALS)
                .collect(Collectors.toList());
        List<Deal> createDeals;
        List<Deal> existingDeals = dealRepository.findAll();

        List<HomeCategory> categoriesWithDeals = existingDeals.stream()
                .map(Deal::getCategory)
                .collect(Collectors.toList());

        List<Deal> newDeals = dealCategories.stream()
                .filter(hc -> !categoriesWithDeals.contains(hc))
                .map(hc -> new Deal(null, hc.getDiscountPercentage(), hc))
                .collect(Collectors.toList());

        if (!newDeals.isEmpty()) {
            existingDeals.addAll(dealRepository.saveAll(newDeals));
        }
        createDeals = existingDeals;

        Home home=new Home();
        home.setGrid(gridCategories);
        home.setShopByCategories(shopByCategories);
        home.setElectricCategories(electricCategories);
        home.setDeals(createDeals);
        home.setDealCategories(dealCategories);
        return home;
    }
}
