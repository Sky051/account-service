package com.akash.accountservice.service;

import com.akash.accountservice.dto.AccountResponse;
import com.akash.accountservice.dto.CreateAccountRequest;
import com.akash.accountservice.entity.Account;
import com.akash.accountservice.entity.AccountStatus;
import com.akash.accountservice.entity.BalanceOperation;
import com.akash.accountservice.entity.OperationType;
import com.akash.accountservice.exception.AccountAlreadyExistsException;
import com.akash.accountservice.exception.AccountNotActiveException;
import com.akash.accountservice.exception.AccountNotFoundException;
import com.akash.accountservice.exception.InsufficientBalanceException;
import com.akash.accountservice.repository.AccountRepository;
import com.akash.accountservice.repository.BalanceOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BalanceOperationRepository balanceOperationRepository;

    public AccountService(AccountRepository accountRepository, BalanceOperationRepository balanceOperationRepository) {
        this.accountRepository = accountRepository;
        this.balanceOperationRepository=balanceOperationRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {

        if (accountRepository
                .findByAccountNumber(request.getAccountNumber())
                .isPresent()) {

            throw new AccountAlreadyExistsException(
                    "Account already exists: " + request.getAccountNumber()
            );
        }

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .balance(request.getInitialBalance())
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);

        return AccountResponse.builder()
                .id(savedAccount.getId())
                .accountNumber(savedAccount.getAccountNumber())
                .balance(savedAccount.getBalance())
                .status(savedAccount.getStatus())
                .build();
    }

    public AccountResponse getAccount(UUID id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found: " + id
                        )
                );

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .build();
    }

    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(account -> AccountResponse.builder()
                        .id(account.getId())
                        .accountNumber(account.getAccountNumber())
                        .balance(account.getBalance())
                        .status(account.getStatus())
                        .build())
                .toList();
    }

    public AccountResponse updateStatus(
            UUID id,
            AccountStatus newStatus) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found: " + id
                        ));

        account.setStatus(newStatus);

        Account updatedAccount = accountRepository.save(account);

        return AccountResponse.builder()
                .id(updatedAccount.getId())
                .accountNumber(updatedAccount.getAccountNumber())
                .balance(updatedAccount.getBalance())
                .status(updatedAccount.getStatus())
                .build();
    }

    @Transactional
    public AccountResponse debit(
            UUID accountId,
            BigDecimal amount,
            UUID operationId
    ) {

        // 1. Check whether this operation was already processed
        Optional<BalanceOperation> existingOperation =
                balanceOperationRepository
                        .findByOperationIdAndOperationType(
                                operationId,
                                OperationType.DEBIT
                        );

        if (existingOperation.isPresent()) {
            return getAccount(accountId);
        }

        // 2. Lock the account
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found: " + accountId
                ));

        // 3. Validate account status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active"
            );
        }

        // 4. Validate balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance"
            );
        }

        // 5. Update balance
        account.setBalance(
                account.getBalance().subtract(amount)
        );

        // 6. Record the operation
        BalanceOperation operation = BalanceOperation.builder()
                .operationId(operationId)
                .operationType(OperationType.DEBIT)
                .accountId(accountId)
                .amount(amount)
                .build();

        balanceOperationRepository.save(operation);

        // 7. Return updated account
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse credit(
            UUID accountId,
            BigDecimal amount,
            UUID operationId
    ) {
        Optional<BalanceOperation> existingOperation =
                balanceOperationRepository.findByOperationIdAndOperationType(
                        operationId,
                        OperationType.CREDIT
                );

        if (existingOperation.isPresent()) {
            return getAccount(accountId);
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found: " + accountId
                        )
                );

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active"
            );
        }

        account.setBalance(
                account.getBalance().add(amount)
        );

        BalanceOperation operation = BalanceOperation.builder()
                .operationId(operationId)
                .operationType(OperationType.CREDIT)
                .accountId(accountId)
                .amount(amount)
                .build();

        balanceOperationRepository.save(operation);

        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus())
                .build();
    }
}