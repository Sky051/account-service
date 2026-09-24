package com.akash.accountservice.controller;
import com.akash.accountservice.dto.BalanceOperationRequest;

import com.akash.accountservice.dto.AccountResponse;
import com.akash.accountservice.dto.CreateAccountRequest;
import com.akash.accountservice.dto.UpdateAccountStatusRequest;
import com.akash.accountservice.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        AccountResponse response =
                accountService.createAccount(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable UUID id) {

        AccountResponse response =
                accountService.getAccount(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {

        return ResponseEntity.ok(
                accountService.getAllAccounts()
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {

        AccountResponse response =
                accountService.updateStatus(
                        id,
                        request.getStatus()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/debit")
    public ResponseEntity<AccountResponse> debit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceOperationRequest request
    ) {
        AccountResponse response = accountService.debit(
                id,
                request.getAmount(),
                request.getOperationId()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/credit")
    public ResponseEntity<AccountResponse> credit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceOperationRequest request
    ) {
        AccountResponse response = accountService.credit(
                id,
                request.getAmount(),
                request.getOperationId()
        );

        return ResponseEntity.ok(response);
    }
}