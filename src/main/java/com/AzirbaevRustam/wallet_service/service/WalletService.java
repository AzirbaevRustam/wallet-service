package com.AzirbaevRustam.wallet_service.service;

import com.AzirbaevRustam.wallet_service.dto.WalletOperationRequest;
import com.AzirbaevRustam.wallet_service.entity.OperationType;
import com.AzirbaevRustam.wallet_service.entity.Wallet;
import com.AzirbaevRustam.wallet_service.exception.InsufficientFundsException;
import com.AzirbaevRustam.wallet_service.exception.WalletNotFoundException;
import com.AzirbaevRustam.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Retryable(
            value = {PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void processOperation(WalletOperationRequest request) {
        UUID walletId = request.getWalletId();
        OperationType operationType = request.getOperationType();
        Long amount = request.getAmount();

        log.info("Processing {} operation for wallet {} with amount {}",
                operationType, walletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseGet(() -> {
                    log.info("Creating new wallet with ID: {}", walletId);
                    return Wallet.builder()
                            .id(walletId)
                            .balance(0L)
                            .build();
                });

        if (operationType == OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance() + amount);
            log.info("Deposited {} to wallet {}. New balance: {}",
                    amount, walletId, wallet.getBalance());
        } else if (operationType == OperationType.WITHDRAW) {
            if (wallet.getBalance() < amount) {
                throw new InsufficientFundsException(walletId);
            }
            wallet.setBalance(wallet.getBalance() - amount);
            log.info("Withdrawn {} from wallet {}. New balance: {}",
                    amount, walletId, wallet.getBalance());
        }

        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        log.info("Retrieved balance for wallet {}: {}", walletId, wallet.getBalance());
        return wallet.getBalance();
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}