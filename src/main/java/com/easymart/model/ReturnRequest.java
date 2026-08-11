package com.easymart.model;

import com.easymart.domain.ReturnStatus;
import com.easymart.domain.ReturnType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class ReturnRequest {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Order order;

    @Enumerated(EnumType.STRING)
    private ReturnType type;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(length = 1000)
    private String sellerNote;

    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;

    @PrePersist
    public void prePersist() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
    }
}
