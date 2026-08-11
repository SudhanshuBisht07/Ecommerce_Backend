package com.easymart.model;

import com.easymart.domain.AccountStatus;
import com.easymart.domain.USER_ROLE;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity

public class Seller {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String sellerName;
    private String mobile;

    @Column(unique=true, nullable=false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Embedded
    private BusinessDetails businessDetails ;

    @Embedded
    private BankDetails bankDetails;

    @OneToOne(cascade = CascadeType.ALL)
    private Address pickupAddress;

    private String GSTIN;

    private USER_ROLE role = USER_ROLE.ROLE_SELLER;

    // Jackson strips the "is" prefix from boolean getters (isEmailVerified() ->
    // "emailVerified" in JSON), which desynced this from the frontend's
    // `isEmailVerified` field (shown as "Unverified" in the admin sellers list
    // even for verified sellers). The getter is declared explicitly with
    // @JsonProperty so the wire format keeps the key the rest of the app expects.
    @Getter(AccessLevel.NONE)
    private boolean isEmailVerified = false;

    private AccountStatus accountStatus= AccountStatus.PENDING_VERIFICATION;

    @JsonProperty("isEmailVerified")
    public boolean isEmailVerified() {
        return isEmailVerified;
    }

}
