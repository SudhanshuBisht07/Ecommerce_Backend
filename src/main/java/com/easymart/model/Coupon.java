package com.easymart.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String code;
    private BigDecimal discountPercentage;
    private LocalDate validityStartDate;
    private LocalDate validityEndDate;
    private BigDecimal minimumOrderValue;
    private boolean isActive=true;
    @ManyToMany(mappedBy = "usedCoupons")
    private Set<User> usedByUsers =new HashSet<>();

}
