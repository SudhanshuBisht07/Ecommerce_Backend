package com.easymart.service.impl;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.PaymentOrderStatus;
import com.easymart.domain.PaymentStatus;
import com.easymart.model.Order;
import com.easymart.model.PaymentOrder;
import com.easymart.model.User;
import com.easymart.repository.OrderRepository;
import com.easymart.repository.PaymentOrderRepository;
import com.easymart.service.PaymentService;
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

    @Value("${razorpay.api.key}")
    private String apiKey;
    @Value("${razorpay.api.secret}")
    private String apiSecret;

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
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) throws RazorpayException {
        if(paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)){
//            RazorpayClient razorpayClient=new RazorpayClient(apiKey, apiSecret);
//            Payment payment=razorpayClient.payments.fetch(paymentId);
//            String status=payment.get("status");
//            if(status.equals("captured")) {
                Set<Order> orders=paymentOrder.getOrders();
                for(Order order:orders){
                    order.setPaymentStatus(PaymentStatus.COMPLETED);
                    orderRepository.save(order);
                }
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                return true;
//            }
//            paymentOrder.setStatus(PaymentOrderStatus.FAILED);
//            paymentOrderRepository.save(paymentOrder);
//            return false;
        }
        return false;
    }

    @Override
    public PaymentLink createRazorpayPaymentLink(User user, Long amount, Long orderId) throws RazorpayException {
        amount=amount*100;
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
            paymentLinkRequest.put("callback_url", "http://localhost:3000/payment-success/"+orderId);
            paymentLinkRequest.put("callback_method","get");

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
}
