package com.easymart.service.impl;

import com.easymart.model.HeroBanner;
import com.easymart.repository.HeroBannerRepository;
import com.easymart.service.HeroBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroBannerServiceImpl implements HeroBannerService {

    private final HeroBannerRepository heroBannerRepository;

    @Override
    public List<HeroBanner> getActiveBanners() {
        return heroBannerRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public List<HeroBanner> getAllBanners() {
        return heroBannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public HeroBanner createBanner(HeroBanner banner) {
        banner.setId(null);
        return heroBannerRepository.save(banner);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public HeroBanner updateBanner(Long id, HeroBanner banner) throws Exception {
        HeroBanner existing = heroBannerRepository.findById(id)
                .orElseThrow(() -> new Exception("Banner not found with id " + id));

        if (banner.getBadgeText() != null) existing.setBadgeText(banner.getBadgeText());
        if (banner.getHeading() != null) existing.setHeading(banner.getHeading());
        if (banner.getHeadingAccent() != null) existing.setHeadingAccent(banner.getHeadingAccent());
        if (banner.getDescription() != null) existing.setDescription(banner.getDescription());
        if (banner.getImageUrl() != null) existing.setImageUrl(banner.getImageUrl());
        if (banner.getPrimaryButtonText() != null) existing.setPrimaryButtonText(banner.getPrimaryButtonText());
        if (banner.getPrimaryButtonLink() != null) existing.setPrimaryButtonLink(banner.getPrimaryButtonLink());
        if (banner.getSecondaryButtonText() != null) existing.setSecondaryButtonText(banner.getSecondaryButtonText());
        if (banner.getSecondaryButtonLink() != null) existing.setSecondaryButtonLink(banner.getSecondaryButtonLink());
        if (banner.getOfferBadgeLabel() != null) existing.setOfferBadgeLabel(banner.getOfferBadgeLabel());
        if (banner.getOfferBadgeValue() != null) existing.setOfferBadgeValue(banner.getOfferBadgeValue());
        if (banner.getStat1Value() != null) existing.setStat1Value(banner.getStat1Value());
        if (banner.getStat1Label() != null) existing.setStat1Label(banner.getStat1Label());
        if (banner.getStat2Value() != null) existing.setStat2Value(banner.getStat2Value());
        if (banner.getStat2Label() != null) existing.setStat2Label(banner.getStat2Label());
        if (banner.getStat3Value() != null) existing.setStat3Value(banner.getStat3Value());
        if (banner.getStat3Label() != null) existing.setStat3Label(banner.getStat3Label());
        if (banner.getDisplayOrder() != null) existing.setDisplayOrder(banner.getDisplayOrder());
        existing.setActive(banner.isActive());

        return heroBannerRepository.save(existing);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Override
    public void deleteBanner(Long id) throws Exception {
        if (!heroBannerRepository.existsById(id)) {
            throw new Exception("Banner not found with id " + id);
        }
        heroBannerRepository.deleteById(id);
    }
}
