package com.easymart.controller;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.ReturnStatus;
import com.easymart.exceptions.SellerException;
import com.easymart.model.Order;
import com.easymart.model.ReturnRequest;
import com.easymart.model.Seller;
import com.easymart.request.ReturnStatusUpdateRequest;
import com.easymart.service.OrderService;
import com.easymart.service.ReturnRequestService;
import com.easymart.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;
    private final SellerService sellerService;
    private final ReturnRequestService returnRequestService;

    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersHandler(
            @RequestHeader("Authorization")String jwt)
        throws SellerException{
        Seller seller=sellerService.getSellerProfile(jwt);
        List<Order> orders=orderService.sellersOrder(seller.getId());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PatchMapping("/{orderId}/status/{orderStatus}")
    public ResponseEntity<Order> updateOrderHandler(
            @RequestHeader("Authorization")String jwt,
            @PathVariable Long orderId,
            @PathVariable OrderStatus orderStatus)
        throws Exception{
        Seller seller = sellerService.getSellerProfile(jwt);
        Order order = orderService.findOrderById(orderId);
        if (!order.getSellerId().equals(seller.getId())) {
            throw new Exception("You don't have access to this order");
        }
        Order updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }

    @GetMapping("/returns")
    public ResponseEntity<List<ReturnRequest>> getReturnsHandler(
            @RequestHeader("Authorization") String jwt) throws SellerException {
        Seller seller = sellerService.getSellerProfile(jwt);
        return ResponseEntity.ok(returnRequestService.getReturnRequestsForSeller(seller));
    }

    @PatchMapping("/returns/{id}/status")
    public ResponseEntity<ReturnRequest> updateReturnStatusHandler(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id,
            @Valid @RequestBody ReturnStatusUpdateRequest request) throws Exception {
        Seller seller = sellerService.getSellerProfile(jwt);
        ReturnRequest updated = returnRequestService.updateReturnRequestStatus(
                id, request.getStatus(), request.getSellerNote(), seller);
        return ResponseEntity.ok(updated);
    }
}
