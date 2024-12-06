package com.spribe.component.currency.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = Currency.TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
public class Currency {

    public static final String TABLE_NAME = "currency";

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)", unique = true, updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false, name = "rate_to_base_currency")
    private BigDecimal rateToBaseCurrency;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
