package com.easymart.service;

import com.easymart.model.HeroBanner;

import java.util.List;

public interface HeroBannerService {
    List<HeroBanner> getActiveBanners();
    List<HeroBanner> getAllBanners();
    HeroBanner createBanner(HeroBanner banner);
    HeroBanner updateBanner(Long id, HeroBanner banner) throws Exception;
    void deleteBanner(Long id) throws Exception;
}
