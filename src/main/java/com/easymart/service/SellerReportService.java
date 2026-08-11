package com.easymart.service;

import com.easymart.model.Seller;
import com.easymart.model.SellerReport;
import com.easymart.response.SalesDataPoint;

import java.util.List;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport(SellerReport sellerReport);
    List<SalesDataPoint> getSalesTimeline(Seller seller, int days);
}
