package com.easymart.repository;

import com.easymart.model.SellerReport;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerReportRepository extends JpaRepository<SellerReport, Long> {
    SellerReport findBySeller_Id(Long sellerId);
}
