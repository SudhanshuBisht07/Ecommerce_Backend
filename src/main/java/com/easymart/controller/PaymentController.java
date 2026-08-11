package com.easymart.controller;

import com.easymart.model.*;
import com.easymart.response.ApiResponse;
import com.easymart.response.PaymentLinkResponse;
import com.easymart.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final OrderService orderService;
    private final TransactionService transactionService;

    @Transactional
    @PostMapping("/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccessHandler(
            @PathVariable String paymentId,
            @RequestParam String paymentLinkId,
            @RequestHeader("Authorization") String jwt)throws Exception{
        User user=userService.findUserByJwtToken(jwt);

        PaymentOrder paymentOrder=paymentService.getPaymentOrderByPaymentId(paymentLinkId);
        boolean paymentSuccess= paymentService.proceedPaymentOrder(
                paymentOrder, paymentId, paymentLinkId);
        if(paymentSuccess){
            ApiResponse res= new ApiResponse();
            res.setMessage("Payment Successful");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        ApiResponse res = new ApiResponse();
        res.setMessage("Payment failed or was not completed");
        return new ResponseEntity<>(res, HttpStatus.PAYMENT_REQUIRED);
    }

    // The Razorpay callback_url is built from the PaymentOrder id (a single
    // checkout can fan out into several per-seller Order rows), so the
    // post-payment redirect needs to resolve orders via this id rather than
    // the /api/orders/{orderId} endpoint, which expects an actual Order id.
    @GetMapping("/{paymentOrderId}/orders")
    public ResponseEntity<java.util.List<Order>> getOrdersForPaymentOrder(
            @PathVariable Long paymentOrderId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        PaymentOrder paymentOrder = paymentService.getPaymentOrderById(paymentOrderId);
        if (!paymentOrder.getUser().getId().equals(user.getId())) {
            throw new Exception("You don't have access to this payment order");
        }
        return ResponseEntity.ok(new java.util.ArrayList<>(paymentOrder.getOrders()));
    }

}
