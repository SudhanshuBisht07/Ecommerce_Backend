package com.easymart.model;

import com.easymart.domain.PaymentMethod;
import com.easymart.domain.PaymentOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity

public class PaymentOrder {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private BigDecimal amount;

    private PaymentOrderStatus status= PaymentOrderStatus.PENDING;

    private PaymentMethod paymentMethod;
    private String paymentLinkId;

    @ManyToOne
    private User user;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "payment_order_orders",
            joinColumns = @JoinColumn(name = "payment_order_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private Set<Order> orders = new HashSet<>();
}
