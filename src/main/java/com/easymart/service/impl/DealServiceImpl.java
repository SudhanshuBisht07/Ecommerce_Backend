package com.easymart.service.impl;

import com.easymart.model.Deal;
import com.easymart.model.HomeCategory;
import com.easymart.repository.DealRepository;
import com.easymart.repository.HomeCategoryRepository;
import com.easymart.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final DealRepository dealRepository;
    private final HomeCategoryRepository homeCategoryRepository;

    @Override
    public List<Deal> getDeals() {
        return dealRepository.findAll();
    }

    @Override
    public Deal createDeal(Deal deal) throws Exception{
        HomeCategory category = homeCategoryRepository.findById(deal.getCategory().getId())
                .orElseThrow(() -> new Exception("HomeCategory not found with id: " + deal.getCategory().getId()));
        deal.setCategory(category);
        return dealRepository.save(deal);
    }

    @Override
    public Deal updateDeal(Deal deal, Long id) throws Exception {
        Deal existingDeal=dealRepository.findById(id).orElse(null);
        HomeCategory category=homeCategoryRepository.findById(deal.getCategory().getId()).orElse(null);

        if(existingDeal!=null){
            if(deal.getDiscount()!=null){
                existingDeal.setDiscount(deal.getDiscount());
            }
            if(category!=null){
                existingDeal.setCategory(category);
            }
            return dealRepository.save(existingDeal);
        }
        throw new Exception("deal not found with id: "+id);
    }

    @Override
    public void deleteDeal(Long id) throws Exception {
        Deal deal=dealRepository.findById(id).orElseThrow(()->new Exception("deal not found with id: "+id));
        dealRepository.delete(deal);
    }
}
