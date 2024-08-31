package com.yellowpepper.challenge.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountTransferDto {

    private String description;
    private String accountRefSource;
    private String accountRefTarget;
    private BigDecimal amount;
    private String currency;
    private Long transferDate;
}
