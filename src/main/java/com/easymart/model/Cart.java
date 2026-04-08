package com.easymart.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    private List<CartItem> cartItems = new ArrayList<>();

    private BigDecimal totalSellingPrice;
    private int totalItems;
    private BigDecimal totalMrpPrice;
    private BigDecimal discount;
    private String couponCode;



}
