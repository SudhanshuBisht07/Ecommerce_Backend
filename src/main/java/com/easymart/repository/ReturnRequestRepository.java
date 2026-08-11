package com.easymart.repository;

import com.easymart.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    ReturnRequest findByOrderId(Long orderId);
    List<ReturnRequest> findByOrderSellerId(Long sellerId);
}
