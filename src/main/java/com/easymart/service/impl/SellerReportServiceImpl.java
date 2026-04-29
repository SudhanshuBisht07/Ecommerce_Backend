package com.easymart.service.impl;

import com.easymart.model.Seller;
import com.easymart.model.SellerReport;
import com.easymart.repository.SellerReportRepository;
import com.easymart.service.SellerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SellerReportServiceImpl implements SellerReportService {

    private final SellerReportRepository sellerReportRepository;

    @Override
    public SellerReport getSellerReport(Seller seller) {
        SellerReport report=sellerReportRepository.findBySeller_Id(seller.getId());

        if(report==null){
            report=new SellerReport();
            report.setSeller(seller);
            report.setTotalOrders(0);
            report.setTotalSales(BigDecimal.ZERO);
            report.setTotalEarnings(BigDecimal.ZERO);
            report.setTotalRefunds(BigDecimal.ZERO);
            report.setCancelledOrders(0);
            return sellerReportRepository.save(report);
        }
        return report;
    }

    @Override
    public SellerReport updateSellerReport(SellerReport sellerReport) {
        return sellerReportRepository.save(sellerReport);
    }
}
