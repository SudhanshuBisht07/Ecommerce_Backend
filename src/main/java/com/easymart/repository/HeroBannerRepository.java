package com.easymart.repository;

import com.easymart.model.HeroBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeroBannerRepository extends JpaRepository<HeroBanner, Long> {
    List<HeroBanner> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<HeroBanner> findAllByOrderByDisplayOrderAsc();
}
