package com.akash.accountservice.repository;

import com.akash.accountservice.entity.BalanceOperation;
import com.akash.accountservice.entity.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BalanceOperationRepository
        extends JpaRepository<BalanceOperation, UUID> {

    Optional<BalanceOperation> findByOperationIdAndOperationType(
            UUID operationId,
            OperationType operationType
    );
}