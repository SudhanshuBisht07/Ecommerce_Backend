package com.easymart.service.impl;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.ReturnStatus;
import com.easymart.domain.ReturnType;
import com.easymart.model.Order;
import com.easymart.model.ReturnRequest;
import com.easymart.model.Seller;
import com.easymart.model.User;
import com.easymart.repository.OrderRepository;
import com.easymart.repository.ReturnRequestRepository;
import com.easymart.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public ReturnRequest createReturnRequest(Long orderId, ReturnType type, String reason, User user) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found with id " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new Exception("You don't have access to this order");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new Exception("Only delivered orders can be returned or exchanged");
        }

        ReturnRequest existing = returnRequestRepository.findByOrderId(orderId);
        if (existing != null && existing.getStatus() != ReturnStatus.REJECTED) {
            throw new Exception("A return or exchange request already exists for this order");
        }

        ReturnRequest request = existing != null ? existing : new ReturnRequest();
        request.setOrder(order);
        request.setType(type);
        request.setReason(reason);
        request.setStatus(ReturnStatus.PENDING);
        request.setSellerNote(null);
        request.setResolvedAt(null);

        return returnRequestRepository.save(request);
    }

    @Override
    public ReturnRequest getReturnRequestByOrderId(Long orderId, User user) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found with id " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new Exception("You don't have access to this order");
        }

        return returnRequestRepository.findByOrderId(orderId);
    }

    @Override
    public List<ReturnRequest> getReturnRequestsForSeller(Seller seller) {
        return returnRequestRepository.findByOrderSellerId(seller.getId());
    }

    @Transactional
    @Override
    public ReturnRequest updateReturnRequestStatus(Long id, ReturnStatus status, String sellerNote, Seller seller) throws Exception {
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new Exception("Return request not found with id " + id));

        if (!request.getOrder().getSellerId().equals(seller.getId())) {
            throw new Exception("You don't have access to this return request");
        }

        request.setStatus(status);
        request.setSellerNote(sellerNote);
        request.setResolvedAt(LocalDateTime.now());

        return returnRequestRepository.save(request);
    }
}
