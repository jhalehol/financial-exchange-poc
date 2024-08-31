package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.service.TransferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/currencies")
public class CommonController {

    private final TransferService transferService;

    public CommonController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping(value = "/supported", produces = "application/json")
    public ResponseEntity<?> getSupportedCurrencies() {
        return ResponseEntity.ok(transferService.getCurrencyTypes());
    }
}
