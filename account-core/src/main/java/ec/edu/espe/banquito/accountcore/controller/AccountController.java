package ec.edu.espe.banquito.accountcore.controller;

import ec.edu.espe.banquito.accountcore.model.Account;
import ec.edu.espe.banquito.accountcore.repository.AccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(acc -> ResponseEntity.ok(Map.of(
                        "accountNumber", acc.getAccountNumber(),
                        "availableBalance", acc.getAvailableBalance(),
                        "status", acc.getStatus()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // TODO: Aquí deberás agregar el @PostMapping("/transfer/p2p") similar al executeDeposit
    // pero restando a una cuenta y sumando a la otra, y notificando a Contabilidad.
}