package com.easymart.service;

import com.easymart.model.Seller;
import com.easymart.model.SellerReport;

public interface SellerReportService {
    SellerReport getSellerReport(Seller seller);
    SellerReport updateSellerReport(SellerReport sellerReport);
}
