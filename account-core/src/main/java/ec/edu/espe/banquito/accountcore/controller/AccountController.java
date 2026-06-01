package ec.edu.espe.banquito.accountcore.controller;

import ec.edu.espe.banquito.accountcore.dto.TransferP2PReqDTO;
import ec.edu.espe.banquito.accountcore.repository.AccountRepository;
import ec.edu.espe.banquito.accountcore.service.AccountTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountTransactionService transactionService;

    public AccountController(AccountRepository accountRepository, AccountTransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(acc -> ResponseEntity.ok(Map.of(
                        "accountNumber", acc.getAccountNumber(),
                        "availableBalance", acc.getAvailableBalance(),
                        "accountingBalance", acc.getAccountingBalance(),
                        "status", acc.getStatus()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/transfer/p2p")
    public ResponseEntity<?> transferP2P(@Valid @RequestBody TransferP2PReqDTO dto) {
        try {
            transactionService.executeP2PTransfer(dto);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Transferencia interna realizada y registrada contablemente.",
                    "uuid", dto.transactionUuid()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getLocalizedMessage()));
        } catch (IllegalStateException e) {
            if ("TRANSACTION_UUID_DUPLICATED".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Transacción rechazada por duplicidad (Idempotencia)."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getLocalizedMessage()));
        } catch (Exception e) {
            // Manejo estricto si falla el microservicio contable secundario [RF-01]
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", "La transferencia fue cancelada debido a una indisponibilidad en el libro mayor contable.",
                    "details", e.getLocalizedMessage()
            ));
        }
    }
}