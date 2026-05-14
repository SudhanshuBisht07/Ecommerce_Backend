package com.easymart.model;

import com.easymart.domain.OrderStatus;
import com.easymart.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "orders")
public class Order {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String orderId;

    @ManyToOne
    private User user;

    private Long sellerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne
    private Address shippingAddress;

    @Embedded
    private PaymentDetails paymentDetails=new PaymentDetails();

    private BigDecimal totalMrpPrice;

    private BigDecimal totalSellingPrice;

    private BigDecimal discount;

    private OrderStatus orderStatus;

    private int totalItem;

    private PaymentStatus paymentStatus =PaymentStatus.PENDING;

    private LocalDateTime orderDate;
    private LocalDateTime deliverDate;
    @PrePersist
    public void prePersist() {
        if (orderDate == null) orderDate = LocalDateTime.now();
        deliverDate = orderDate.plusDays(7);
        if (orderId == null) orderId = "ORD-" + System.currentTimeMillis();
    }
}
