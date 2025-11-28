package com.AzirbaevRustam.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {

    private UUID walletId;
    private Long balance;
}