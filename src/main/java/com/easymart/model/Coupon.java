package com.easymart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Coupon {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String code;
    private BigDecimal discountPercentage;
    private LocalDate validityStartDate;
    private LocalDate validityEndDate;
    private BigDecimal minimumOrderValue;

    // Jackson strips the "is" prefix from boolean accessors (isActive() ->
    // JSON key "active", setActive() -> JSON key "active" too), which
    // desynced this from the frontend's `isActive` field in BOTH directions:
    // reads came back as "active" (so `coupon.isActive` was always
    // undefined), and writes from the admin toggle's `{ isActive: ... }`
    // payload weren't binding to this field at all. Both accessors are
    // pinned to the "isActive" JSON key explicitly; the Java method names
    // (isActive()/setActive()) are kept as-is since other services call them.
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean isActive = true;

    @ManyToMany(mappedBy = "usedCoupons")
    private Set<User> usedByUsers =new HashSet<>();

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("isActive")
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

}
