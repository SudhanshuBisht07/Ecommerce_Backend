package com.easymart.service.impl;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.PaymentOrderStatus;
import com.easymart.domain.PaymentStatus;
import com.easymart.model.*;
import com.easymart.repository.OrderRepository;
import com.easymart.repository.PaymentOrderRepository;
import com.easymart.repository.ProductRepository;
import com.easymart.service.PaymentService;
import com.easymart.service.SellerReportService;
import com.easymart.service.SellerService;
import com.easymart.service.TransactionService;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final TransactionService transactionService;

    @Value("${razorpay.api.key}")
    private String apiKey;
    @Value("${razorpay.api.secret}")
    private String apiSecret;
    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public PaymentOrder createOrder(User user, Set<Order> orders) {
        BigDecimal amount = orders.stream()
                .map(Order::getTotalSellingPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PaymentOrder paymentOrder=new PaymentOrder();
        paymentOrder.setAmount(amount);
        paymentOrder.setUser(user);
        paymentOrder.setOrders(orders);
        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long orderId) throws Exception {
        return paymentOrderRepository.findById(orderId)
                .orElseThrow(()->new Exception("payment order not found"));
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String paymentLinkId) throws Exception {
        PaymentOrder paymentOrder=paymentOrderRepository.findByPaymentLinkId(paymentLinkId);
        if(paymentOrder==null){
            throw new Exception("payment order not found with payment link id");
        }
        return paymentOrder;
    }

    @Override
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) throws RazorpayException, Exception {
        // Idempotency guard: PaymentSuccessPage's effect can fire twice for
        // the same payment (React StrictMode double-invokes effects in dev,
        // and a page refresh re-runs it too). Previously a second call saw a
        // non-PENDING status and fell through to `return false`, so a
        // payment that had *already succeeded* on the first call would show
        // the "couldn't confirm this payment" error on the second. Treat an
        // already-SUCCESS payment order as still successful.
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.SUCCESS)) {
            return true;
        }
        if(paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)){
            RazorpayClient razorpayClient=new RazorpayClient(apiKey, apiSecret);
            Payment payment=razorpayClient.payments.fetch(paymentId);
            String status=payment.get("status");
            if(status.equals("captured")) {
                Set<Order> orders=paymentOrder.getOrders();
                for(Order order:orders){
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    order.setOrderStatus(OrderStatus.PLACED);
                    // Was never being populated at all, so the order
                    // details page always showed blank "Payment ID" and
                    // "Razorpay Link" fields even for completed orders.
                    order.getPaymentDetails().setPaymentId(paymentId);
                    order.getPaymentDetails().setRazorpayPaymentLinkId(paymentLinkId);
                    order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order);
                }
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                // Only runs on the actual first-time transition into SUCCESS
                // above (the idempotency guard at the top returns early on
                // any later re-confirmation), so transactions/seller reports
                // are never double-counted.
                finalizeOrderCompletion(paymentOrder);
                return true;
            }
            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
            paymentOrderRepository.save(paymentOrder);
            return false;
        }
        return false;
    }

    @Override
    public PaymentLink createRazorpayPaymentLink(User user, Long amount, Long orderId) throws RazorpayException {
        try{
            RazorpayClient razorpayClient=new RazorpayClient(apiKey, apiSecret);

            JSONObject paymentLinkRequest=new JSONObject();
            paymentLinkRequest.put("amount", amount);
            paymentLinkRequest.put("currency", "INR");

            JSONObject customer=new JSONObject();
            customer.put("name", user.getFullName());
            customer.put("email", user.getEmail());

            paymentLinkRequest.put("customer", customer);

            JSONObject notify=new JSONObject();
            notify.put("email", true);

            paymentLinkRequest.put("notify", notify);
            paymentLinkRequest.put("callback_url", frontendBaseUrl + "/payment-success/" + orderId);
            paymentLinkRequest.put("callback_method", "get");

            PaymentLink payment=razorpayClient.paymentLink.create(paymentLinkRequest);
            return  payment;
        }
        catch(Exception e){
            throw new RazorpayException(e.getMessage());
        }
    }

    @Override
    public PaymentOrder updatePaymentOrder(PaymentOrder paymentOrder) {
        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public void finalizeOrderCompletion(PaymentOrder paymentOrder) throws Exception {
        for (Order order : paymentOrder.getOrders()) {
            transactionService.createTransaction(order);
            Seller seller = sellerService.getSellerById(order.getSellerId());
            SellerReport report = sellerReportService.getSellerReport(seller);

            // order.getTotalSellingPrice() is the actual, coupon-adjusted
            // amount for this order (set correctly in
            // OrderServiceImpl.createOrder). Each OrderItem.sellingPrice,
            // by contrast, is the PRE-COUPON line price — the coupon
            // discount was only ever subtracted at the order's aggregate
            // level, never redistributed back onto individual items. Summing
            // profit from item.getSellingPrice() therefore overstated profit
            // by the coupon amount whenever one was applied. Deriving from
            // the order total instead nets that out correctly.
            BigDecimal totalWholesaleCost = BigDecimal.ZERO;
            for (OrderItem item : order.getOrderItems()) {
                BigDecimal wholesalePrice = item.getWholesalePrice() != null
                        ? item.getWholesalePrice()
                        : BigDecimal.ZERO;
                // wholesalePrice is per-unit; sellingPrice line totals
                // (and thus order.getTotalSellingPrice()) are already
                // quantity-multiplied, so match that here.
                totalWholesaleCost = totalWholesaleCost.add(wholesalePrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            BigDecimal orderProfit = order.getTotalSellingPrice().subtract(totalWholesaleCost);

            report.setTotalOrders(report.getTotalOrders() + 1);
            report.setTotalEarnings(report.getTotalEarnings().add(order.getTotalSellingPrice()));
            report.setTotalSales(report.getTotalSales().add(order.getTotalSellingPrice()));
            report.setNetEarnings(report.getNetEarnings().add(orderProfit));
            sellerReportService.updateSellerReport(report);
        }
    }
}
