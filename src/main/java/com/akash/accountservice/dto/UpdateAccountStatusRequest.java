package com.akash.accountservice.dto;

import com.akash.accountservice.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAccountStatusRequest {

    @NotNull
    private AccountStatus status;
}