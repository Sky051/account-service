package com.akash.accountservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class BalanceOperationRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private UUID operationId;
}