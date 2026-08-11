package com.easymart.service;

import com.easymart.domain.ReturnStatus;
import com.easymart.domain.ReturnType;
import com.easymart.model.ReturnRequest;
import com.easymart.model.Seller;
import com.easymart.model.User;

import java.util.List;

public interface ReturnRequestService {
    ReturnRequest createReturnRequest(Long orderId, ReturnType type, String reason, User user) throws Exception;
    ReturnRequest getReturnRequestByOrderId(Long orderId, User user) throws Exception;
    List<ReturnRequest> getReturnRequestsForSeller(Seller seller);
    ReturnRequest updateReturnRequestStatus(Long id, ReturnStatus status, String sellerNote, Seller seller) throws Exception;
}
