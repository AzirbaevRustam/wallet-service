package com.AzirbaevRustam.wallet_service.controller;

import com.AzirbaevRustam.wallet_service.dto.WalletBalanceResponse;
import com.AzirbaevRustam.wallet_service.dto.WalletOperationRequest;
import com.AzirbaevRustam.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<Void> processWalletOperation(
            @RequestBody @Valid WalletOperationRequest request) {

        log.info("Received wallet operation request: {}", request);
        walletService.processOperation(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletBalanceResponse> getWalletBalance(
            @PathVariable UUID walletId) {

        log.info("Received balance request for wallet: {}", walletId);
        Long balance = walletService.getBalance(walletId);

        WalletBalanceResponse response = new WalletBalanceResponse(walletId, balance);
        return ResponseEntity.ok(response);
    }
}