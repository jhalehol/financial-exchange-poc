package com.yellowpepper.challenge.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferItemDto {

    private String relatedAccount;
    private BigDecimal originalAmount;
    private String originalCurrency;
    private BigDecimal finalAmount;
    private String finalCurrency;
    private String observations;
    private String description;
    private Long transferDate;
    private String userRelatedAccount;
}
