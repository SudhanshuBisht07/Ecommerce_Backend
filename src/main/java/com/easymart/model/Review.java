package com.easymart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Review {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String reviewText;

    @Column(nullable = false)
    private double rating;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> productImages;

    // Was missing entirely — the frontend rendered `new Date(review.createdAt)`
    // against a field that never existed, always showing "Invalid Date".
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(nullable = false)
    private Product product;

    // Reviews are served from a public, unauthenticated endpoint
    // (GET /api/products/{id}/reviews), so the full User entity — email,
    // mobile, addresses — shouldn't be exposed to every visitor just to
    // show a reviewer's name and avatar.
    @JsonIgnoreProperties({"email", "mobile", "addresses", "role", "usedCoupons"})
    @ManyToOne
    @JoinColumn( nullable = false)
    private User user;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

}
