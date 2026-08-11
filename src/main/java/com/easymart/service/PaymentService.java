package com.easymart.service;

import com.easymart.model.Order;
import com.easymart.model.PaymentOrder;
import com.easymart.model.User;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayException;

import java.util.Set;

public interface PaymentService {
    PaymentOrder createOrder(User user, Set<Order> orders);
    PaymentOrder getPaymentOrderById(Long orderId) throws Exception;
    PaymentOrder getPaymentOrderByPaymentId(String paymentLinkId) throws Exception;
    Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) throws RazorpayException, Exception;
    PaymentLink createRazorpayPaymentLink(User user, Long amount, Long orderId) throws RazorpayException;
    PaymentOrder updatePaymentOrder(PaymentOrder paymentOrder);

    // Marks every order in the payment as paid/placed and updates seller
    // transactions + reports. Shared by the normal Razorpay success callback
    // and by orders that are fully covered by a coupon (nothing to charge,
    // so there's no Razorpay round trip to confirm in the first place).
    void finalizeOrderCompletion(PaymentOrder paymentOrder) throws Exception;
}
