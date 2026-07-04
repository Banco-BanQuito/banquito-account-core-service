package ec.edu.espe.banquito.core.accountcore.client;

import ec.edu.espe.banquito.core.accountcore.dto.AccountingOperationReqDTO;
import ec.edu.espe.banquito.core.accountcore.enums.AccountingOperationType;
import ec.edu.espe.banquito.core.accountcore.enums.AccountingProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountingServiceClientIntegrationTests {

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_ACCOUNTING_INTEGRATION", matches = "true")
    void postsFunctionalOperationInAccountingService() {
        AccountingServiceClient client = new AccountingServiceClient("localhost", 9092);

        try {
            var response = assertDoesNotThrow(() -> client.postOperation(new AccountingOperationReqDTO(
                            testUuid("deposit"),
                            AccountingOperationType.TELLER_DEPOSIT,
                            AccountingProductType.SAVINGS,
                            null,
                            new BigDecimal("10.00"),
                            null,
                            "Integration test from account-core-service",
                            LocalDate.of(2026, Month.JUNE, 11)
                    ))
            );
            assertAll(
                    () -> assertEquals(0, BigDecimal.ZERO.compareTo(response.commissionAmount())),
                    () -> assertEquals(0, BigDecimal.ZERO.compareTo(response.ivaAmount())),
                    () -> assertEquals(new BigDecimal("10.00"), response.totalDebited())
            );
        } finally {
            client.shutdown();
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_ACCOUNTING_INTEGRATION", matches = "true")
    void postsCrossProductP2PTransfer() {
        AccountingServiceClient client = new AccountingServiceClient("localhost", 9092);

        try {
            var response = assertDoesNotThrow(() -> client.postOperation(new AccountingOperationReqDTO(
                    testUuid("p2p"),
                    AccountingOperationType.P2P_TRANSFER,
                    AccountingProductType.SAVINGS,
                    AccountingProductType.CHECKING,
                    new BigDecimal("12.00"),
                    BigDecimal.ZERO,
                    "Cross-product integration test",
                    LocalDate.of(2026, Month.JUNE, 11)
            )));
            assertEquals(new BigDecimal("12.00"), response.totalDebited());
        } finally {
            client.shutdown();
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_ACCOUNTING_INTEGRATION", matches = "true")
    void receivesCommissionVatAndTotalFromAccountingService() {
        AccountingServiceClient client = new AccountingServiceClient("localhost", 9092);

        try {
            var response = assertDoesNotThrow(() -> client.postOperation(new AccountingOperationReqDTO(
                    testUuid("corporate"),
                    AccountingOperationType.CORPORATE_DEBIT,
                    AccountingProductType.CHECKING,
                    null,
                    new BigDecimal("100.00"),
                    new BigDecimal("10.00"),
                    "Corporate debit integration test",
                    LocalDate.of(2026, Month.JUNE, 11)
            )));
            assertAll(
                    () -> assertEquals(new BigDecimal("10.00"), response.commissionAmount()),
                    () -> assertEquals(new BigDecimal("1.50"), response.ivaAmount()),
                    () -> assertEquals(new BigDecimal("111.50"), response.totalDebited())
            );
        } finally {
            client.shutdown();
        }
    }

    private String testUuid(String operation) {
        String prefix = System.getProperty("test.entry.uuid", UUID.randomUUID().toString());
        return UUID.nameUUIDFromBytes((prefix + operation).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
