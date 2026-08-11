package com.easymart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity

public class HeroBanner {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String badgeText;
    private String heading;
    private String headingAccent;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private String primaryButtonText;
    private String primaryButtonLink;
    private String secondaryButtonText;
    private String secondaryButtonLink;

    private String offerBadgeLabel;
    private String offerBadgeValue;

    private String stat1Value;
    private String stat1Label;
    private String stat2Value;
    private String stat2Label;
    private String stat3Value;
    private String stat3Label;

    private Integer displayOrder;

    // Jackson strips the "is" prefix from boolean accessors (isActive() ->
    // JSON key "active", setActive() -> JSON key "active" too), which
    // desynced this from the frontend's `isActive` field in BOTH directions.
    // Both accessors are pinned to the "isActive" JSON key explicitly; the
    // Java method names (isActive()/setActive()) are kept as-is since other
    // services call them.
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean isActive = true;

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("isActive")
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
