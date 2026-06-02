package ec.edu.espe.banquito.accountcore.controller;

import ec.edu.espe.banquito.accountcore.dto.BatchCreditReqDTO;
import ec.edu.espe.banquito.accountcore.dto.BatchCreditResponseDTO;
import ec.edu.espe.banquito.accountcore.dto.CorporateDebitReqDTO;
import ec.edu.espe.banquito.accountcore.dto.CorporateDebitResponseDTO;
import ec.edu.espe.banquito.accountcore.service.AccountTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/payments")
public class CorePaymentIntegrationController {

    private final AccountTransactionService transactionService;

    public CorePaymentIntegrationController(AccountTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/batch-credit")
    public ResponseEntity<BatchCreditResponseDTO> batchCredit(@Valid @RequestBody BatchCreditReqDTO request) {
        return ResponseEntity.ok(transactionService.executeBatchCredit(request));
    }

    @PostMapping("/corporate-debit")
    public ResponseEntity<CorporateDebitResponseDTO> corporateDebit(@Valid @RequestBody CorporateDebitReqDTO request) {
        return ResponseEntity.ok(transactionService.executeCorporateDebit(request));
    }
}
