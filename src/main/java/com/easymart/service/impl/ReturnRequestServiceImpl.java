package com.easymart.service.impl;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.ReturnStatus;
import com.easymart.domain.ReturnType;
import com.easymart.model.Order;
import com.easymart.model.OrderItem;
import com.easymart.model.ReturnRequest;
import com.easymart.model.Seller;
import com.easymart.model.SellerReport;
import com.easymart.model.User;
import com.easymart.repository.OrderRepository;
import com.easymart.repository.ReturnRequestRepository;
import com.easymart.service.ReturnRequestService;
import com.easymart.service.SellerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReturnRequestServiceImpl implements ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final SellerReportService sellerReportService;

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

        ReturnStatus previousStatus = request.getStatus();

        request.setStatus(status);
        request.setSellerNote(sellerNote);
        request.setResolvedAt(LocalDateTime.now());

        ReturnRequest saved = returnRequestRepository.save(request);

        // A completed RETURN undoes the sale — the seller dashboard
        // (profit/sales/order count) needs to reflect that instead of still
        // counting it as a completed sale. EXCHANGE keeps the sale (a
        // different item ships out), so it isn't reversed. Guarded on
        // previousStatus so re-saving an already-completed request doesn't
        // subtract twice.
        if (status == ReturnStatus.COMPLETED
                && previousStatus != ReturnStatus.COMPLETED
                && request.getType() == ReturnType.RETURN) {
            reverseOrderFromSellerReport(request.getOrder(), seller);
        }

        return saved;
    }

    private void reverseOrderFromSellerReport(Order order, Seller seller) {
        BigDecimal orderProfit = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            BigDecimal wholesalePrice = item.getWholesalePrice() != null
                    ? item.getWholesalePrice()
                    : BigDecimal.ZERO;
            BigDecimal itemWholesaleTotal = wholesalePrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            orderProfit = orderProfit.add(item.getSellingPrice().subtract(itemWholesaleTotal));
        }

        SellerReport report = sellerReportService.getSellerReport(seller);
        report.setTotalOrders(Math.max(0, report.getTotalOrders() - 1));
        report.setTotalEarnings(report.getTotalEarnings().subtract(order.getTotalSellingPrice()));
        report.setTotalSales(report.getTotalSales().subtract(order.getTotalSellingPrice()));
        report.setNetEarnings(report.getNetEarnings().subtract(orderProfit));
        report.setTotalRefunds(report.getTotalRefunds().add(order.getTotalSellingPrice()));
        sellerReportService.updateSellerReport(report);
    }
}
