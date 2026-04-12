package com.easymart.controller;

import com.easymart.model.*;
import com.easymart.response.PaymentLinkResponse;
import com.easymart.service.*;
import com.razorpay.PaymentLink;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final PaymentService paymentService;



    @PostMapping
    public ResponseEntity<PaymentLinkResponse> createOrderHandler(
            @RequestBody Address shippingAddress,
            @RequestHeader("Authorization") String jwt)
        throws Exception{

        User user=userService.findUserByJwtToken(jwt);
        Cart cart=cartService.findUserCart(user);
        Set<Order> orders=orderService.createOrder(user, shippingAddress, cart);

        PaymentOrder paymentOrder=paymentService.createOrder(user, orders);

        PaymentLinkResponse paymentLinkResponse=new PaymentLinkResponse();

//        PaymentLink payment=paymentService.createRazorpayPaymentLink(user,
//                    paymentOrder.getAmount().longValue(), paymentOrder.getId());
//        String paymentUrl=payment.get("short_url");
//        String paymentUrlId=payment.get("id");
//        paymentLinkResponse.setPayment_link_url(paymentUrl);
//        paymentOrder.setPaymentLinkId(paymentUrlId);
//        paymentService.updatePaymentOrder(paymentOrder);
        paymentLinkResponse.setPayment_link_url("http://localhost:3000/payment-success/" + paymentOrder.getId());
        return new ResponseEntity<>(paymentLinkResponse, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> userOrderHistoryHandler(
            @RequestHeader("Authorization") String jwt)
        throws Exception{

        User user= userService.findUserByJwtToken(jwt);
        List<Order> orders=orderService.userOrderHistory(user.getId());
        return new ResponseEntity<>(orders, HttpStatus.ACCEPTED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization") String jwt)
        throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        Order order=orderService.findOrderById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new Exception("You don't have access to this order");
        }
        return new ResponseEntity<>(order, HttpStatus.ACCEPTED);
    }
    @GetMapping("/item/{orderItemId}")
    public ResponseEntity<OrderItem> getOrderItemById(
            @PathVariable Long orderItemId,
            @RequestHeader("Authorization")String jwt)
        throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        OrderItem orderItem=orderService.getOrderItemById(orderItemId);
        if (!orderItem.getUserId().equals(user.getId())) {
            throw new Exception("You don't have access to this order item");
        }
        return new ResponseEntity<>(orderItem, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization")String jwt)
        throws Exception{

        User user=userService.findUserByJwtToken(jwt);
        Order order=orderService.cancelOrder(orderId, user);

       Seller seller=sellerService.getSellerById(order.getSellerId());
       SellerReport sellerReport=sellerReportService.getSellerReport(seller);

       sellerReport.setCancelledOrders(sellerReport.getCancelledOrders()+1);
        sellerReport.setTotalRefunds(sellerReport.getTotalRefunds().add(order.getTotalSellingPrice()));
       sellerReportService.updateSellerReport(sellerReport);

        return ResponseEntity.ok(order);
    }

}
