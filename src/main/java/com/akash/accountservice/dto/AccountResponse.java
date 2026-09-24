package com.akash.accountservice.dto;

import com.akash.accountservice.entity.AccountStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class AccountResponse {

    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountStatus status;
}