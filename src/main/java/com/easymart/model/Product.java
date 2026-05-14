package com.easymart.model;

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

public class Product {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String brand;
    private BigDecimal mrpPrice;
    private BigDecimal sellingPrice;
    private int discountPercent;
    private int quantity;
    private String color;

    @ElementCollection(fetch = FetchType.EAGER) //creates seperate table for this
    private List<String> images = new ArrayList<>();
    private int numRatings;
    @Column(nullable = false)
    private Double avgRatings = 0.0;

    @ManyToOne
    private Category category;

    @ManyToOne
    private Seller seller;

    private LocalDateTime createdAt;

    private String size;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Review> reviews = new ArrayList<>();




}
