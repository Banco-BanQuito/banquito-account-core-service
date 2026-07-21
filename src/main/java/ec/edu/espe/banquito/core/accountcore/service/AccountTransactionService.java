package ec.edu.espe.banquito.core.accountcore.service;

import ec.edu.espe.banquito.core.accountcore.client.AccountingServiceClient;
import ec.edu.espe.banquito.core.accountcore.client.PartyServiceClient;
import ec.edu.espe.banquito.core.accountcore.client.NotificationGrpcClient;
import ec.edu.espe.banquito.core.accountcore.dto.AccountingOperationReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.AccountingOperationResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.BatchCreditReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.BatchCreditResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.CorporateDebitReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.CorporateDebitResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.CorporateRefundReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.CorporateRefundResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.ExternalTransferReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.ExternalTransferResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.OffUsSettlementReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.OffUsSettlementResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.OperationResponseDTO;
import ec.edu.espe.banquito.core.accountcore.dto.TellerTransactionReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.TransactionHistoryDTO;
import ec.edu.espe.banquito.core.accountcore.dto.TransferP2PReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.TransferResponseDTO;
import ec.edu.espe.banquito.core.accountcore.enums.AccountStatus;
import ec.edu.espe.banquito.core.accountcore.enums.AccountSuperType;
import ec.edu.espe.banquito.core.accountcore.enums.AccountingOperationType;
import ec.edu.espe.banquito.core.accountcore.enums.AccountingProductType;
import ec.edu.espe.banquito.core.accountcore.enums.TransactionStatus;
import ec.edu.espe.banquito.core.accountcore.enums.TransactionSubtypeCode;
import ec.edu.espe.banquito.core.accountcore.enums.TransactionType;
import ec.edu.espe.banquito.core.accountcore.exception.AccountNotFoundException;
import ec.edu.espe.banquito.core.accountcore.exception.DuplicateTransactionException;
import ec.edu.espe.banquito.core.accountcore.domain.AccountDomainService;
import ec.edu.espe.banquito.core.accountcore.exception.InactiveAccountException;
import ec.edu.espe.banquito.core.accountcore.model.Account;
import ec.edu.espe.banquito.core.accountcore.model.AccountTransaction;
import ec.edu.espe.banquito.core.accountcore.model.TransactionSubtype;
import ec.edu.espe.banquito.core.accountcore.repository.AccountRepository;
import ec.edu.espe.banquito.core.accountcore.repository.AccountTransactionRepository;
import ec.edu.espe.banquito.core.accountcore.repository.TransactionSubtypeRepository;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AccountTransactionService {

    private static final ZoneId BANK_ZONE = ZoneId.of("America/Guayaquil");
    private static final BigDecimal EXTERNAL_TRANSFER_COMMISSION = new BigDecimal("0.60");

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final TransactionSubtypeRepository transactionSubtypeRepository;
    private final AccountingServiceClient accountingServiceClient;
    private final PartyServiceClient partyServiceClient;
    private final NotificationGrpcClient notificationGrpcClient;
    private final AccountingDateService accountingDateService;
    private final TransactionTemplate localMovementTx;
    private final TransactionTemplate compensationTx;

    public AccountTransactionService(AccountRepository accountRepository,
                                     AccountTransactionRepository transactionRepository,
                                     TransactionSubtypeRepository transactionSubtypeRepository,
                                     AccountingServiceClient accountingServiceClient,
                                     PartyServiceClient partyServiceClient,
                                     NotificationGrpcClient notificationGrpcClient,
                                     AccountingDateService accountingDateService,
                                     PlatformTransactionManager transactionManager) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionSubtypeRepository = transactionSubtypeRepository;
        this.accountingServiceClient = accountingServiceClient;
        this.partyServiceClient = partyServiceClient;
        this.notificationGrpcClient = notificationGrpcClient;
        this.accountingDateService = accountingDateService;
        this.localMovementTx = new TransactionTemplate(transactionManager);
        this.compensationTx = new TransactionTemplate(transactionManager);
        this.compensationTx.setPropagationBehavior(
                org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryDTO getTransactionHistory(Long accountId, LocalDate from, LocalDate to, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        Page<AccountTransaction> page = transactionRepository.findHistory(accountId, from, to, pageable);
        List<TransactionHistoryDTO.TransactionHistoryItemDTO> content = page.getContent().stream()
                .map(transaction -> new TransactionHistoryDTO.TransactionHistoryItemDTO(
                        transaction.getTransactionUuid(),
                        transaction.getMovementType().name(),
                        transaction.getAmount(),
                        transaction.getResultingBalance(),
                        transaction.getTransactionDate(),
                        transaction.getAccountingDate(),
                        transaction.getDescription()
                ))
                .toList();
        return new TransactionHistoryDTO(content, page.getTotalElements(), page.getNumber());
    }

    public OperationResponseDTO executeDeposit(TellerTransactionReqDTO request) {
        validateIdempotency(request.transactionUuid());
        LocalDate accountingDate = accountingDateService.resolveAccountingDate();

        LocalMovementResult result = localMovementTx.execute(status -> {
            Account account = getAccountForUpdate(request.accountId());
            validateActiveAccount(account);
            partyServiceClient.validateActiveCustomer(account.getCustomerId());

            AccountDomainService.credit(account, request.amount());
            accountRepository.save(account);

            AccountTransaction transaction = transactionRepository.save(createTransaction(
                    account,
                    new TransactionCreationData(
                            request.amount(),
                            TransactionType.CREDITO,
                            TransactionSubtypeCode.DEP_VEN,
                            request.transactionUuid(),
                            accountingDate,
                            account.getAvailableBalance(),
                            descriptionOrDefault(request.reference(), "Teller deposit")
                    )
            ));
            return new LocalMovementResult(account, transaction, TransactionSubtypeCode.DEP_REV);
        });

        try {
            accountingServiceClient.postOperation(new AccountingOperationReqDTO(
                    request.transactionUuid(),
                    AccountingOperationType.TELLER_DEPOSIT,
                    getAccountingProductType(result.account()),
                    null,
                    request.amount(),
                    null,
                    descriptionOrDefault(request.reference(), "Teller deposit account " + result.account().getId()),
                    accountingDate
            ));
        } catch (StatusRuntimeException e) {
            compensateLocalMovement(result, "executeDeposit", e);
            throw e;
        }

        notifyTransaction(result.account(), TransactionType.CREDITO, request.amount(), result.account().getAvailableBalance());

        return toOperationResponse(result.transaction(), result.account().getAvailableBalance());
    }

    public OperationResponseDTO executeWithdrawal(TellerTransactionReqDTO request) {
        validateIdempotency(request.transactionUuid());
        LocalDate accountingDate = accountingDateService.resolveAccountingDate();

        LocalMovementResult result = localMovementTx.execute(status -> {
            Account account = getAccountForUpdate(request.accountId());
            validateActiveAccount(account);
            partyServiceClient.validateActiveCustomer(account.getCustomerId());
            AccountDomainService.validateSufficientBalance(account, request.amount());

            AccountDomainService.debit(account, request.amount());
            accountRepository.save(account);

            AccountTransaction transaction = transactionRepository.save(createTransaction(
                    account,
                    new TransactionCreationData(
                            request.amount(),
                            TransactionType.DEBITO,
                            TransactionSubtypeCode.RET_VEN,
                            request.transactionUuid(),
                            accountingDate,
                            account.getAvailableBalance(),
                            descriptionOrDefault(request.reference(), "Teller withdrawal")
                    )
            ));
            return new LocalMovementResult(account, transaction, TransactionSubtypeCode.RET_REV);
        });

        try {
            accountingServiceClient.postOperation(new AccountingOperationReqDTO(
                    request.transactionUuid(),
                    AccountingOperationType.TELLER_WITHDRAWAL,
                    getAccountingProductType(result.account()),
                    null,
                    request.amount(),
                    null,
                    descriptionOrDefault(request.reference(), "Teller withdrawal account " + result.account().getId()),
                    accountingDate
            ));
        } catch (StatusRuntimeException e) {
            compensateLocalMovement(result, "executeWithdrawal", e);
            throw e;
        }

        notifyTransaction(result.account(), TransactionType.DEBITO, request.amount(), result.account().getAvailableBalance());

        return toOperationResponse(result.transaction(), result.account().getAvailableBalance());
    }

    public TransferResponseDTO executeP2PTransfer(TransferP2PReqDTO request) {
        validateIdempotency(request.transactionUuid());
        LocalDate accountingDate = accountingDateService.resolveAccountingDate();

        P2PLocalMovementResult result = localMovementTx.execute(status -> {
            Account sourceAccount = getAccountForUpdate(request.originAccountId());
            Account destinationAccount = getAccountForUpdate(request.destinationAccountNumber());

            if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber())) {
                throw new IllegalArgumentException("Source and destination accounts must be different");
            }

            validateActiveAccount(sourceAccount);
            validateActiveAccount(destinationAccount);
            partyServiceClient.validateActiveCustomer(sourceAccount.getCustomerId());
            partyServiceClient.validateActiveCustomer(destinationAccount.getCustomerId());
            AccountDomainService.validateSufficientBalance(sourceAccount, request.amount());

            AccountDomainService.debit(sourceAccount, request.amount());
            AccountDomainService.credit(destinationAccount, request.amount());
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            AccountTransaction debitTransaction = transactionRepository.save(createTransaction(
                    sourceAccount,
                    new TransactionCreationData(
                            request.amount(),
                            TransactionType.DEBITO,
                            TransactionSubtypeCode.TRF_P2P_S,
                            request.transactionUuid(),
                            accountingDate,
                            sourceAccount.getAvailableBalance(),
                            descriptionOrDefault(request.reference(), "Internal P2P transfer sent")
                    )
            ));
            AccountTransaction creditTransaction = transactionRepository.save(createTransaction(
                    destinationAccount,
                    new TransactionCreationData(
                            request.amount(),
                            TransactionType.CREDITO,
                            TransactionSubtypeCode.TRF_P2P_E,
                            request.transactionUuid(),
                            accountingDate,
                            destinationAccount.getAvailableBalance(),
                            descriptionOrDefault(request.reference(), "Internal P2P transfer received")
                    )
            ));
            return new P2PLocalMovementResult(sourceAccount, debitTransaction, destinationAccount, creditTransaction);
        });

        try {
            accountingServiceClient.postOperation(new AccountingOperationReqDTO(
                    request.transactionUuid(),
                    AccountingOperationType.P2P_TRANSFER,
                    getAccountingProductType(result.sourceAccount()),
                    getAccountingProductType(result.destinationAccount()),
                    request.amount(),
                    BigDecimal.ZERO,
                    descriptionOrDefault(
                            request.reference(),
                            "Internal P2P transfer " + result.sourceAccount().getAccountNumber()
                                    + " to " + result.destinationAccount().getAccountNumber()),
                    accountingDate
            ));
        } catch (StatusRuntimeException e) {
            compensateP2PLocalMovement(result, e);
            throw e;
        }

        return new TransferResponseDTO(
                result.debitTransaction().getTransactionUuid(),
                result.sourceAccount().getAvailableBalance(),
                result.destinationAccount().getAccountNumber(),
                partyServiceClient.getHolderNameByAccount(result.destinationAccount().getAccountNumber()),
                result.debitTransaction().getStatus(),
                result.debitTransaction().getAccountingDate()
        );
    }

    public BatchCreditResponseDTO executeBatchCredit(BatchCreditReqDTO request) {
        List<BatchCreditResponseDTO.BatchCreditResultDTO> results = new ArrayList<>();
        int failedCount = 0;

        for (BatchCreditReqDTO.CreditItemDTO creditItem : request.credits()) {
            validateIdempotency(creditItem.transactionUuid());
            LocalDate accountingDate = accountingDateService.resolveAccountingDate();

            LocalMovementResult result = localMovementTx.execute(status -> {
                Account account = getAccountForUpdate(creditItem.accountNumber());
                validateActiveAccount(account);
                partyServiceClient.validateActiveCustomer(account.getCustomerId());

                AccountDomainService.credit(account, creditItem.amount());
                accountRepository.save(account);

                AccountTransaction transaction = transactionRepository.save(createTransaction(
                        account,
                        new TransactionCreationData(
                                creditItem.amount(),
                                TransactionType.CREDITO,
                                TransactionSubtypeCode.PAG_NOM_C,
                                creditItem.transactionUuid(),
                                accountingDate,
                                account.getAvailableBalance(),
                                descriptionOrDefault(creditItem.reference(), "Abono de nómina, lote " + request.batchId())
                        )
                ));
                return new LocalMovementResult(account, transaction, TransactionSubtypeCode.PAG_NOM_C_REV);
            });

            try {
                accountingServiceClient.postOperation(new AccountingOperationReqDTO(
                        creditItem.transactionUuid(),
                        AccountingOperationType.BATCH_CREDIT,
                        getAccountingProductType(result.account()),
                        null,
                        creditItem.amount(),
                        BigDecimal.ZERO,
                        descriptionOrDefault(
                                creditItem.reference(),
                                "Abono de nómina, lote " + request.batchId()),
                        accountingDate
                ));
            } catch (StatusRuntimeException e) {
                compensateLocalMovement(result, "executeBatchCredit", e);
                failedCount++;
                results.add(new BatchCreditResponseDTO.BatchCreditResultDTO(
                        creditItem.accountNumber(),
                        "FAILED",
                        creditItem.transactionUuid()
                ));
                continue;
            }

            results.add(new BatchCreditResponseDTO.BatchCreditResultDTO(
                    creditItem.accountNumber(),
                    "SUCCESS",
                    creditItem.transactionUuid()
            ));
        }

        return new BatchCreditResponseDTO(request.batchId(), results.size(), failedCount, results);
    }

    @Transactional
    public CorporateDebitResponseDTO executeCorporateDebit(CorporateDebitReqDTO request) {
        validateIdempotency(request.transactionUuid());

        Account account = getAccountForUpdate(request.accountNumber());
        validateActiveAccount(account);
        partyServiceClient.validateActiveCustomer(account.getCustomerId());

        LocalDate accountingDate = accountingDateService.resolveAccountingDate();
        AccountingOperationResponseDTO accountingResult = accountingServiceClient.postOperation(
                new AccountingOperationReqDTO(
                        request.transactionUuid(),
                        AccountingOperationType.CORPORATE_DEBIT,
                        getAccountingProductType(account),
                        null,
                        request.totalAmount(),
                        request.commissionAmount(),
                        "Débito de lote de nómina, lote " + request.batchId(),
                        accountingDate
                )
        );

        AccountTransaction transaction;
        try {
            AccountDomainService.validateSufficientBalance(account, accountingResult.totalDebited());
            AccountDomainService.debit(account, accountingResult.totalDebited());
            accountRepository.save(account);

            transaction = transactionRepository.save(createTransaction(
                    account,
                    new TransactionCreationData(
                            accountingResult.totalDebited(),
                            TransactionType.DEBITO,
                            TransactionSubtypeCode.DEB_EMP,
                            request.transactionUuid(),
                            accountingDate,
                            account.getAvailableBalance(),
                            "Débito de lote de nómina, lote " + request.batchId()
                    )
            ));
        } catch (RuntimeException e) {
            compensateAccounting(accountingResult.entryUuid(), "executeCorporateDebit", e);
            throw e;
        }

        return new CorporateDebitResponseDTO(
                transaction.getTransactionUuid(),
                accountingResult.totalDebited(),
                accountingResult.commissionAmount(),
                accountingResult.ivaAmount(),
                transaction.getStatus(),
                transaction.getAccountingDate()
        );
    }

    @Transactional
    public ExternalTransferResponseDTO executeExternalTransfer(ExternalTransferReqDTO request) {
        validateIdempotency(request.transactionUuid());

        Account account = getAccountForUpdate(request.originAccountId());
        validateActiveAccount(account);
        partyServiceClient.validateActiveCustomer(account.getCustomerId());

        LocalDate accountingDate = accountingDateService.resolveAccountingDate();
        AccountingOperationResponseDTO accountingResult = accountingServiceClient.postOperation(
                new AccountingOperationReqDTO(
                        request.transactionUuid(),
                        AccountingOperationType.EXTERNAL_TRANSFER,
                        getAccountingProductType(account),
                        null,
                        request.amount(),
                        EXTERNAL_TRANSFER_COMMISSION,
                        descriptionOrDefault(
                                request.reference(),
                                "Transferencia interbancaria a " + request.externalBankName()
                                        + " cuenta " + request.externalAccountNumber()),
                        accountingDate
                )
        );

        AccountTransaction transaction;
        try {
            AccountDomainService.validateSufficientBalance(account, accountingResult.totalDebited());
            AccountDomainService.debit(account, accountingResult.totalDebited());
            accountRepository.save(account);

            transaction = transactionRepository.save(createTransaction(
                    account,
                    new TransactionCreationData(
                            accountingResult.totalDebited(),
                            TransactionType.DEBITO,
                            TransactionSubtypeCode.TRF_EXT_S,
                            request.transactionUuid(),
                            accountingDate,
                            account.getAvailableBalance(),
                            descriptionOrDefault(
                                    request.reference(),
                                    "Transferencia interbancaria a " + request.externalBankName()
                                            + " cuenta " + request.externalAccountNumber())
                    )
            ));
        } catch (RuntimeException e) {
            compensateAccounting(accountingResult.entryUuid(), "executeExternalTransfer", e);
            throw e;
        }

        return new ExternalTransferResponseDTO(
                transaction.getTransactionUuid(),
                account.getAccountNumber(),
                request.amount(),
                accountingResult.commissionAmount(),
                accountingResult.ivaAmount(),
                accountingResult.totalDebited(),
                account.getAvailableBalance(),
                transaction.getStatus(),
                transaction.getAccountingDate()
        );
    }

    public CorporateRefundResponseDTO executeCorporateRefund(CorporateRefundReqDTO request) {
        validateIdempotency(request.transactionUuid());
        LocalDate accountingDate = accountingDateService.resolveAccountingDate();

        LocalMovementResult result = localMovementTx.execute(status -> {
            Account account = getAccountForUpdate(request.accountNumber());
            validateActiveAccount(account);
            partyServiceClient.validateActiveCustomer(account.getCustomerId());
            AccountDomainService.credit(account, request.refundAmount());
            accountRepository.save(account);

            AccountTransaction transaction = transactionRepository.save(createTransaction(
                    account,
                    new TransactionCreationData(
                            request.refundAmount(),
                            TransactionType.CREDITO,
                            TransactionSubtypeCode.DEV_EMP,
                            request.transactionUuid(),
                            accountingDate,
                            account.getAvailableBalance(),
                            "Devolución de líneas rechazadas, lote " + request.batchId()
                    )
            ));
            return new LocalMovementResult(account, transaction, TransactionSubtypeCode.DEV_EMP_REV);
        });

        try {
            accountingServiceClient.postOperation(new AccountingOperationReqDTO(
                    request.transactionUuid(),
                    AccountingOperationType.CORPORATE_REFUND,
                    getAccountingProductType(result.account()),
                    request.refundAmount(),
                    BigDecimal.ZERO,
                    "Devolución de líneas rechazadas, lote " + request.batchId(),
                    accountingDate
            ));
        } catch (StatusRuntimeException e) {
            compensateLocalMovement(result, "executeCorporateRefund", e);
            throw e;
        }

        return new CorporateRefundResponseDTO(
                result.transaction().getTransactionUuid(),
                request.refundAmount(),
                result.transaction().getStatus(),
                result.transaction().getAccountingDate()
        );
    }

    @Transactional
    public OffUsSettlementResponseDTO executeOffUsSettlement(OffUsSettlementReqDTO request) {
        // No customer Account is involved here, so idempotency relies on accounting-service's
        // own entryUuid dedup (registerEntry returns the existing entry for a repeated UUID).
        LocalDate accountingDate = accountingDateService.resolveAccountingDate();
        accountingServiceClient.postOperation(new AccountingOperationReqDTO(
                request.transactionUuid(),
                AccountingOperationType.OFFUS_SETTLEMENT,
                null,
                request.amount(),
                BigDecimal.ZERO,
                "Liquidación de compensación interbancaria, lote " + request.batchId(),
                accountingDate
        ));

        return new OffUsSettlementResponseDTO(
                request.transactionUuid(),
                request.amount(),
                TransactionStatus.COMPLETADA,
                accountingDate
        );
    }

    /**
     * RF-01: si el trabajo local falla despues de que accounting-service ya registro
     * el asiento, se compensa deshaciendolo alla explicitamente en vez de depender
     * de que el rollback local (que no alcanza esa base de datos separada) lo arregle solo.
     */
    private void compensateAccounting(String entryUuid, String context, RuntimeException cause) {
        try {
            accountingServiceClient.reverseOperation(entryUuid);
            log.warn("[RF-01][COMPENSACION] Asiento {} reversado tras fallo local en {}: {}",
                    entryUuid, context, cause.getMessage());
        } catch (Exception ex) {
            log.error("[RF-01][COMPENSACION-FALLIDA] Asiento {} en {} requiere reconciliacion manual: {}",
                    entryUuid, context, ex.getMessage());
        }
    }

    /**
     * RF-01: si postOperation rechaza el asiento (ej. Suma Cero) o falla por timeout/red
     * despues de que el movimiento local ya fue commiteado en su propia transaccion, se
     * compensa deshaciendolo aqui explicitamente en vez de depender de un rollback que ya
     * no es posible (el trabajo local y la llamada a accounting-service no comparten
     * transaccion de base de datos). Corre en una transaccion NUEVA (REQUIRES_NEW) e
     * idempotente respecto a transactionUuid + subtipo de reverso, por si executeX se
     * reintenta tras una compensacion previa.
     */
    private void compensateLocalMovement(LocalMovementResult result, String context, RuntimeException cause) {
        String transactionUuid = result.transaction().getTransactionUuid();
        String reversalCode = result.reversalSubtype().name();
        try {
            compensationTx.executeWithoutResult(status -> {
                if (transactionRepository.existsByTransactionUuidAndTransactionSubtype_Code(transactionUuid, reversalCode)) {
                    return;
                }
                Account account = getAccountForUpdate(result.account().getId());
                reverseMovement(account, result.transaction());
                accountRepository.save(account);
                transactionRepository.save(createReversalTransaction(
                        account, result.transaction(), result.reversalSubtype()));
            });
            log.warn("[RF-01][COMPENSACION] Movimiento local {} (cuenta {}) reversado tras fallo en accounting durante {}: {}",
                    transactionUuid, result.account().getAccountNumber(), context, cause.getMessage());
        } catch (Exception ex) {
            log.error("[RF-01][COMPENSACION-FALLIDA] Movimiento local {} (cuenta {}) en {} requiere reconciliacion manual: {}",
                    transactionUuid, result.account().getAccountNumber(), context, ex.getMessage());
        }
    }

    /**
     * Variante de compensateLocalMovement para executeP2PTransfer: un solo asiento contable
     * cubre dos movimientos locales (debito origen + credito destino), asi que ambos se
     * reversan juntos dentro de la misma transaccion de compensacion.
     */
    private void compensateP2PLocalMovement(P2PLocalMovementResult result, RuntimeException cause) {
        String transactionUuid = result.debitTransaction().getTransactionUuid();
        try {
            compensationTx.executeWithoutResult(status -> {
                if (transactionRepository.existsByTransactionUuidAndTransactionSubtype_Code(
                        transactionUuid, TransactionSubtypeCode.TRF_P2P_S_REV.name())) {
                    return;
                }
                Account sourceAccount = getAccountForUpdate(result.sourceAccount().getId());
                Account destinationAccount = getAccountForUpdate(result.destinationAccount().getId());

                reverseMovement(sourceAccount, result.debitTransaction());
                reverseMovement(destinationAccount, result.creditTransaction());
                accountRepository.save(sourceAccount);
                accountRepository.save(destinationAccount);

                transactionRepository.save(createReversalTransaction(
                        sourceAccount, result.debitTransaction(), TransactionSubtypeCode.TRF_P2P_S_REV));
                transactionRepository.save(createReversalTransaction(
                        destinationAccount, result.creditTransaction(), TransactionSubtypeCode.TRF_P2P_E_REV));
            });
            log.warn("[RF-01][COMPENSACION] Transferencia P2P {} (origen {}, destino {}) reversada tras fallo en accounting durante executeP2PTransfer: {}",
                    transactionUuid, result.sourceAccount().getAccountNumber(),
                    result.destinationAccount().getAccountNumber(), cause.getMessage());
        } catch (Exception ex) {
            log.error("[RF-01][COMPENSACION-FALLIDA] Transferencia P2P {} (origen {}, destino {}) en executeP2PTransfer requiere reconciliacion manual: {}",
                    transactionUuid, result.sourceAccount().getAccountNumber(),
                    result.destinationAccount().getAccountNumber(), ex.getMessage());
        }
    }

    private void reverseMovement(Account account, AccountTransaction originalTransaction) {
        if (originalTransaction.getMovementType() == TransactionType.CREDITO) {
            AccountDomainService.debit(account, originalTransaction.getAmount());
        } else {
            AccountDomainService.credit(account, originalTransaction.getAmount());
        }
    }

    private AccountTransaction createReversalTransaction(Account account,
                                                          AccountTransaction originalTransaction,
                                                          TransactionSubtypeCode reversalSubtype) {
        TransactionType reversalType = originalTransaction.getMovementType() == TransactionType.CREDITO
                ? TransactionType.DEBITO
                : TransactionType.CREDITO;
        AccountTransaction reversal = createTransaction(
                account,
                new TransactionCreationData(
                        originalTransaction.getAmount(),
                        reversalType,
                        reversalSubtype,
                        originalTransaction.getTransactionUuid(),
                        originalTransaction.getAccountingDate(),
                        account.getAvailableBalance(),
                        "RF-01 Reverso automatico: " + originalTransaction.getDescription()
                )
        );
        reversal.setStatus(TransactionStatus.REVERSADA);
        return reversal;
    }

    private record LocalMovementResult(
            Account account,
            AccountTransaction transaction,
            TransactionSubtypeCode reversalSubtype
    ) {}

    private record P2PLocalMovementResult(
            Account sourceAccount,
            AccountTransaction debitTransaction,
            Account destinationAccount,
            AccountTransaction creditTransaction
    ) {}

    private void validateIdempotency(String transactionUuid) {
        LocalDateTime from = LocalDateTime.now(BANK_ZONE).minusDays(1);
        if (transactionRepository.existsByTransactionUuidAndTransactionDateAfter(transactionUuid, from)) {
            throw new DuplicateTransactionException(transactionUuid);
        }
    }
    private Account getAccountForUpdate(Long accountId) {
        Account account = accountRepository.findWithLockById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.getAccountSubtype().getSuperType();
        return account;
    }
    private Account getAccountForUpdate(String accountNumber) {
        Account account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        account.getAccountSubtype().getSuperType();
        return account;
    }
    private void validateActiveAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVA) {
            throw new InactiveAccountException(account.getAccountNumber());
        }
    }
    private AccountTransaction createTransaction(Account account, TransactionCreationData data) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setAmount(data.amount());
        transaction.setMovementType(data.transactionType());
        transaction.setTransactionSubtype(getTransactionSubtype(data.transactionSubtype()));
        transaction.setTransactionUuid(data.transactionUuid());
        transaction.setAccountingDate(data.accountingDate());
        transaction.setResultingBalance(data.resultingBalance());
        transaction.setStatus(TransactionStatus.COMPLETADA);
        transaction.setDescription(data.description());
        return transaction;
    }
    private OperationResponseDTO toOperationResponse(AccountTransaction transaction, BigDecimal newBalance) {
        return new OperationResponseDTO(
                transaction.getTransactionUuid(),
                transaction.getAccountingDate(),
                newBalance,
                transaction.getStatus(),
                LocalDateTime.now(BANK_ZONE)
        );
    }
    private void notifyTransaction(Account account, TransactionType type, BigDecimal amount, BigDecimal newBalance) {
        try {
            String email = this.partyServiceClient.getCustomerEmail(account.getCustomerId());
            if (email == null || email.isBlank()) {
                return;
            }
            String customerName = this.partyServiceClient.getHolderNameByAccount(account.getAccountNumber());
            this.notificationGrpcClient.sendNotification(
                    email,
                    "BanQuito - " + (type == TransactionType.CREDITO ? "Depósito" : "Retiro") + " en tu cuenta " + account.getAccountNumber(),
                    "TRANSACTION_EXECUTED",
                    Map.of(
                            "customerName", customerName != null ? customerName : "",
                            "accountNumber", account.getAccountNumber(),
                            "transactionType", type.name(),
                            "amount", amount.toPlainString(),
                            "newBalance", newBalance.toPlainString(),
                            "date", LocalDate.now(BANK_ZONE).toString()
                    )
            );
        } catch (Exception ignored) {
        }
    }
    private AccountingProductType getAccountingProductType(Account account) {
        AccountSuperType superType = account.getAccountSubtype().getSuperType();
        return switch (superType) {
            case AHORROS -> AccountingProductType.SAVINGS;
            case CORRIENTE -> AccountingProductType.CHECKING;
        };
    }
    private TransactionSubtype getTransactionSubtype(TransactionSubtypeCode subtypeCode) {
        return transactionSubtypeRepository.findByCode(subtypeCode.name())
                .orElseThrow(() -> new IllegalStateException("Transaction subtype is not configured: " + subtypeCode.name()));
    }
    private String descriptionOrDefault(String description, String defaultDescription) {
        return description == null || description.isBlank() ? defaultDescription : description;
    }
    private record TransactionCreationData(
            BigDecimal amount,
            TransactionType transactionType,
            TransactionSubtypeCode transactionSubtype,
            String transactionUuid,
            LocalDate accountingDate,
            BigDecimal resultingBalance,
            String description
    ) {}
}
