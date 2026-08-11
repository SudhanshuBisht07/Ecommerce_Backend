package com.easymart.controller;

import com.easymart.model.HeroBanner;
import com.easymart.service.HeroBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hero-banners")
public class HeroBannerController {

    private final HeroBannerService heroBannerService;

    @GetMapping
    public ResponseEntity<List<HeroBanner>> getActiveBanners() {
        return ResponseEntity.ok(heroBannerService.getActiveBanners());
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<HeroBanner>> getAllBanners() {
        return ResponseEntity.ok(heroBannerService.getAllBanners());
    }

    @PostMapping("/admin")
    public ResponseEntity<HeroBanner> createBanner(@RequestBody HeroBanner banner) {
        return new ResponseEntity<>(heroBannerService.createBanner(banner), HttpStatus.CREATED);
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<HeroBanner> updateBanner(@PathVariable Long id, @RequestBody HeroBanner banner) throws Exception {
        return ResponseEntity.ok(heroBannerService.updateBanner(id, banner));
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteBanner(@PathVariable Long id) throws Exception {
        heroBannerService.deleteBanner(id);
        return ResponseEntity.ok().build();
    }
}
