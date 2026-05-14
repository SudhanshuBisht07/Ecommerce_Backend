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
            for(Order order:paymentOrder.getOrders()){
                transactionService.createTransaction(order);
                Seller seller=sellerService.getSellerById(order.getSellerId());
                SellerReport report=sellerReportService.getSellerReport(seller);
                report.setTotalOrders(report.getTotalOrders()+1);
                report.setTotalEarnings(report.getTotalEarnings().add(order.getTotalSellingPrice()));
                report.setTotalSales(report.getTotalSales().add(BigDecimal.valueOf(order.getOrderItems().size())));
                sellerReportService.updateSellerReport(report);
            }
            ApiResponse res= new ApiResponse();
            res.setMessage("Payment Successful");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        ApiResponse res = new ApiResponse();
        res.setMessage("Payment failed or was not completed");
        return new ResponseEntity<>(res, HttpStatus.PAYMENT_REQUIRED);
    }

}
