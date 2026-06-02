package ec.edu.espe.banquito.accountcore.controller;

import ec.edu.espe.banquito.accountcore.dto.BalanceResponseDTO;
import ec.edu.espe.banquito.accountcore.dto.HealthResponseDTO;
import ec.edu.espe.banquito.accountcore.dto.OperationResponseDTO;
import ec.edu.espe.banquito.accountcore.dto.TellerTransactionReqDTO;
import ec.edu.espe.banquito.accountcore.dto.TransactionHistoryDTO;
import ec.edu.espe.banquito.accountcore.dto.TransferP2PReqDTO;
import ec.edu.espe.banquito.accountcore.dto.TransferResponseDTO;
import ec.edu.espe.banquito.accountcore.exception.AccountNotFoundException;
import ec.edu.espe.banquito.accountcore.repository.AccountRepository;
import ec.edu.espe.banquito.accountcore.service.AccountTransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v2/accounts")
public class AccountController {

    private static final String CURRENCY = "USD";

    private final AccountRepository accountRepository;
    private final AccountTransactionService transactionService;

    public AccountController(AccountRepository accountRepository, AccountTransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponseDTO> getBalance(@PathVariable Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> ResponseEntity.ok(new BalanceResponseDTO(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getAvailableBalance(),
                        account.getAccountingBalance(),
                        account.getStatus(),
                        CURRENCY
                )))
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<TransactionHistoryDTO> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountId, from, to, pageable));
    }

    @PostMapping("/teller/deposit")
    public ResponseEntity<OperationResponseDTO> tellerDeposit(@Valid @RequestBody TellerTransactionReqDTO request) {
        return ResponseEntity.ok(transactionService.executeDeposit(request));
    }

    @PostMapping("/teller/withdrawal")
    public ResponseEntity<OperationResponseDTO> tellerWithdrawal(@Valid @RequestBody TellerTransactionReqDTO request) {
        return ResponseEntity.ok(transactionService.executeWithdrawal(request));
    }

    @PostMapping("/transfer/p2p")
    public ResponseEntity<TransferResponseDTO> transferP2P(@Valid @RequestBody TransferP2PReqDTO request) {
        return ResponseEntity.ok(transactionService.executeP2PTransfer(request));
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponseDTO> health() {
        return ResponseEntity.ok(new HealthResponseDTO("UP", "account-core-service", "2.0"));
    }
}
