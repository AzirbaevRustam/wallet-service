package com.AzirbaevRustam.wallet_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDepositToNewWallet() throws Exception {
        UUID walletId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "walletId": "%s",
                                    "operationType": "DEPOSIT",
                                    "amount": 1000
                                }
                                """.formatted(walletId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        UUID walletId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "walletId": "%s",
                                    "operationType": "DEPOSIT",
                                    "amount": 2000
                                }
                                """.formatted(walletId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "walletId": "%s",
                                    "operationType": "WITHDRAW",
                                    "amount": 500
                                }
                                """.formatted(walletId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void shouldReturn404WhenWalletNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallets/{walletId}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenMissingWalletId() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "operationType": "DEPOSIT",
                                    "amount": 1000
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn422WhenInsufficientFunds() throws Exception {
        UUID walletId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "walletId": "%s",
                                    "operationType": "DEPOSIT",
                                    "amount": 100
                                }
                                """.formatted(walletId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "walletId": "%s",
                                    "operationType": "WITHDRAW",
                                    "amount": 1000
                                }
                                """.formatted(walletId)))
                .andExpect(status().isUnprocessableEntity());
    }
}