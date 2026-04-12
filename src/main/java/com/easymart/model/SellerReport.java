package com.easymart.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity

public class SellerReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne
    private Seller seller;

    private BigDecimal totalEarnings = BigDecimal.ZERO;
    private BigDecimal totalSales = BigDecimal.ZERO;
    private BigDecimal totalRefunds = BigDecimal.ZERO;
    private BigDecimal totalTax = BigDecimal.ZERO;
    private BigDecimal netEarnings = BigDecimal.ZERO;
    private Integer totalOrders=0;
    private Integer cancelledOrders=0;
    private Integer totalTransactions=0;


}
