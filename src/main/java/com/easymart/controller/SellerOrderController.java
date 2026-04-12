package com.easymart.controller;

import com.easymart.domain.OrderStatus;
import com.easymart.exceptions.SellerException;
import com.easymart.model.Order;
import com.easymart.model.Seller;
import com.easymart.service.OrderService;
import com.easymart.service.SellerService;
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

    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersHandler(
            @RequestHeader("Authorization")String jwt)
        throws SellerException{
        Seller seller=sellerService.getSellerProfile(jwt);
        List<Order> orders=orderService.sellersOrder(seller.getId());
        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
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
        return new ResponseEntity<>(updatedOrder, HttpStatus.ACCEPTED);
    }


}
