package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.AccountTransferDto;
import com.yellowpepper.challenge.financial.dto.AccountTransfersPageDto;
import com.yellowpepper.challenge.financial.dto.TransferResultDto;
import com.yellowpepper.challenge.financial.dto.TransfersFilterDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping(value = "/{accountRef}/{startDate}/{endDate}/{pageNumber}/{pageSize}", produces = "application/json")
    public ResponseEntity<?> getAccountTransfers(@PathVariable String accountRef,
            @PathVariable Long startDate, @PathVariable Long endDate,
            @PathVariable Integer pageNumber, @PathVariable Integer pageSize) {
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .accountRef(accountRef)
                .startDate(startDate)
                .endDate(endDate)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build();
        try {
            final AccountTransfersPageDto transfersPage = transferService.getAccountTransfers(filterDto);
            return ResponseEntity.ok(transfersPage);
        } catch (InvalidArgumentsException e) {
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Unable to get transfer accounts: %s", e.getMessage()));
        }

    }

    @PostMapping(value = "/populate", produces = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> populateTransfers(@RequestBody List<AccountTransferDto> transfersList)
    {
        try {
            final List<TransferResultDto> transfers = transferService.populateTransfers(transfersList);
            return ResponseEntity.ok(transfers);
        } catch (InvalidArgumentsException e) {
            return ResponseEntity
                    .badRequest()
                    .body(String.format("Unable to populate list of transfers: %s", e.getMessage()));
        }
    }

}
