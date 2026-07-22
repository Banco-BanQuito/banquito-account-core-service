package ec.edu.espe.banquito.core.accountcore.client;

import ec.edu.espe.banquito.core.accountcore.dto.AccountingOperationReqDTO;
import ec.edu.espe.banquito.core.accountcore.dto.AccountingOperationResponseDTO;
import ec.edu.espe.banquito.core.accountcore.grpc.accounting.AccountingEntryResponse;
import ec.edu.espe.banquito.core.accountcore.grpc.accounting.AccountingOperationRequest;
import ec.edu.espe.banquito.core.accountcore.grpc.accounting.AccountingServiceGrpc;
import ec.edu.espe.banquito.core.accountcore.grpc.accounting.ReverseOperationRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class AccountingServiceClient {

    private final ManagedChannel channel;
    private final AccountingServiceGrpc.AccountingServiceBlockingStub accountingService;

    public AccountingServiceClient(
            @Value("${accounting.grpc.host:localhost}") String accountingGrpcHost,
            @Value("${accounting.grpc.port:9092}") int accountingGrpcPort) {
        this.channel = ManagedChannelBuilder
                .forAddress(accountingGrpcHost, accountingGrpcPort)
                .usePlaintext()
                .build();
        this.accountingService = AccountingServiceGrpc.newBlockingStub(channel);
    }

    public AccountingOperationResponseDTO postOperation(AccountingOperationReqDTO request) {
        AccountingEntryResponse response = accountingService
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .postOperation(toGrpcRequest(request));
        return toResponse(response);
    }

    /**
     * RF-01: compensacion explicita. El Core llama esto para deshacer un asiento que
     * accounting-service ya registro, cuando el propio trabajo local del Core falla
     * despues de esa llamada (bases de datos separadas, el rollback local no alcanza
     * al lado remoto).
     */
    public void reverseOperation(String entryUuid) {
        accountingService
                .withDeadlineAfter(5, TimeUnit.SECONDS)
                .reverseOperation(ReverseOperationRequest.newBuilder().setEntryUuid(entryUuid).build());
    }

    @PreDestroy
    public void shutdown() {
        channel.shutdown();
    }

    private AccountingOperationRequest toGrpcRequest(AccountingOperationReqDTO request) {
        AccountingOperationRequest.Builder builder = AccountingOperationRequest.newBuilder()
                .setOperationUuid(request.operationUuid())
                .setOperationType(request.operationType().name())
                .setAmount(request.amount().toPlainString())
                .setReference(request.reference() == null ? "" : request.reference())
                .setAccountingDate(request.accountingDate().toString());
        if (request.sourceAccountProductType() != null) {
            builder.setSourceAccountProductType(request.sourceAccountProductType().name());
        }
        if (request.destinationAccountProductType() != null) {
            builder.setDestinationAccountProductType(request.destinationAccountProductType().name());
        }
        if (request.commissionAmount() != null) {
            builder.setCommissionAmount(request.commissionAmount().toPlainString());
        }
        if (request.ivaAmount() != null && request.ivaAmount().compareTo(BigDecimal.ZERO) > 0) {
            builder.setIvaAmount(request.ivaAmount().toPlainString());
        }
        if (request.sourceAccountNumber() != null && !request.sourceAccountNumber().isBlank()) {
            builder.setSourceAccountNumber(request.sourceAccountNumber());
        }
        if (request.destinationAccountNumber() != null && !request.destinationAccountNumber().isBlank()) {
            builder.setDestinationAccountNumber(request.destinationAccountNumber());
        }
        if (request.beneficiaryName() != null && !request.beneficiaryName().isBlank()) {
            builder.setBeneficiaryName(request.beneficiaryName());
        }
        return builder.build();
    }

    private AccountingOperationResponseDTO toResponse(AccountingEntryResponse response) {
        return new AccountingOperationResponseDTO(
                response.getEntryId(),
                response.getEntryUuid(),
                response.getStatus(),
                response.getValidationResult(),
                LocalDateTime.parse(response.getRegisteredAt()),
                new BigDecimal(response.getCommissionAmount()),
                new BigDecimal(response.getIvaAmount()),
                new BigDecimal(response.getTotalDebited())
        );
    }
}
