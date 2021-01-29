package com.bank.mhub.model;

import lombok.*;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Getter
@Setter
public class Payment {
    private BigDecimal amount;
    private String currency;
    private String accountNumber;
    private String paymentType;
    private String paymentMethod;
}
