package com.yellowpepper.challenge.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyTypeDto {

    private String currencyCode;
    private String symbol;
    private String diplayName;
}
