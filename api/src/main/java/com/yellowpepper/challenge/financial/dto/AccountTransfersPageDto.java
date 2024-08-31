package com.yellowpepper.challenge.financial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransfersPageDto {

    private Integer totalPages;
    private Long totalElements;
    private List<TransferItemDto> transfers;
}
