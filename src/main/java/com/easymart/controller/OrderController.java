package com.easymart.controller;

import com.easymart.domain.PaymentStatus;
import com.easymart.model.*;
import com.easymart.request.CreateReturnRequest;
import com.easymart.response.PaymentLinkResponse;
import com.easymart.service.*;
import com.razorpay.PaymentLink;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final ReturnRequestService returnRequestService;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Transactional
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
        long amountInPaise = paymentOrder.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // A coupon (e.g. a 100%-off code) can bring the payable amount to
        // zero. Razorpay can't create a ₹0 payment link, and there's nothing
        // to actually charge, so mark the order paid directly and skip the
        // Razorpay round trip instead of sending the user into a payment
        // flow that would fail.
        if (amountInPaise <= 0) {
            paymentOrder.setStatus(com.easymart.domain.PaymentOrderStatus.SUCCESS);
            for (Order order : orders) {
                order.setPaymentStatus(PaymentStatus.COMPLETED);
                order.setOrderStatus(com.easymart.domain.OrderStatus.PLACED);
                // No Razorpay round trip happens for a fully-covered order,
                // so there's no real payment id/link — label it clearly
                // instead of leaving the order details page blank.
                order.getPaymentDetails().setPaymentId("FREE_ORDER");
                order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
            }
            orderService.saveAll(orders);
            paymentService.updatePaymentOrder(paymentOrder);
            paymentService.finalizeOrderCompletion(paymentOrder);

            paymentLinkResponse.setPayment_link_url(frontendBaseUrl + "/orders");
            paymentLinkResponse.setPayment_link_id(null);
            return new ResponseEntity<>(paymentLinkResponse, HttpStatus.OK);
        }

        PaymentLink payment=paymentService.createRazorpayPaymentLink(user, amountInPaise, paymentOrder.getId());
        String paymentUrl=payment.get("short_url");
        String paymentUrlId=payment.get("id");
        paymentLinkResponse.setPayment_link_url(paymentUrl);
        paymentLinkResponse.setPayment_link_id(paymentUrlId);
        paymentOrder.setPaymentLinkId(paymentUrlId);
        paymentService.updatePaymentOrder(paymentOrder);
        return new ResponseEntity<>(paymentLinkResponse, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> userOrderHistoryHandler(
            @RequestHeader("Authorization") String jwt)
        throws Exception{

        User user= userService.findUserByJwtToken(jwt);
        List<Order> orders=orderService.userOrderHistory(user.getId());
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization") String jwt)
        throws Exception{
        User user=userService.findUserByJwtToken(jwt);
        Order order=orderService.findOrderById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new Exception("You don't have access to this order");
        }
        return new ResponseEntity<>(order, HttpStatus.OK);
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
        return new ResponseEntity<>(orderItem, HttpStatus.OK);
    }
    @Transactional
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
        if(order.getPaymentStatus() == PaymentStatus.COMPLETED){
            sellerReport.setTotalRefunds(sellerReport.getTotalRefunds().add(order.getTotalSellingPrice()));
            sellerReport.setTotalEarnings(
                    sellerReport.getTotalEarnings().subtract(order.getTotalSellingPrice()));
            sellerReport.setTotalSales(
                    sellerReport.getTotalSales().subtract(order.getTotalSellingPrice()));

            // order.getTotalSellingPrice() is the coupon-adjusted total that
            // was actually added to earnings/sales above; OrderItem.sellingPrice
            // is a pre-coupon LINE TOTAL (not per-unit), so subtracting a
            // per-unit wholesalePrice from it and then multiplying by
            // quantity again both ignored the coupon and double-counted
            // quantity.
            BigDecimal totalWholesaleCost = BigDecimal.ZERO;
            for (OrderItem item : order.getOrderItems()) {
                BigDecimal wholesalePrice = item.getWholesalePrice() != null
                        ? item.getWholesalePrice()
                        : BigDecimal.ZERO;
                totalWholesaleCost = totalWholesaleCost.add(wholesalePrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            BigDecimal orderProfit = order.getTotalSellingPrice().subtract(totalWholesaleCost);
            sellerReport.setNetEarnings(sellerReport.getNetEarnings().subtract(orderProfit));
        }
        sellerReportService.updateSellerReport(sellerReport);

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<ReturnRequest> createReturnRequestHandler(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReturnRequest request,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        ReturnRequest returnRequest = returnRequestService.createReturnRequest(
                orderId, request.getType(), request.getReason(), user);
        return new ResponseEntity<>(returnRequest, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}/return")
    public ResponseEntity<ReturnRequest> getReturnRequestHandler(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        ReturnRequest returnRequest = returnRequestService.getReturnRequestByOrderId(orderId, user);
        return ResponseEntity.ok(returnRequest);
    }

}
