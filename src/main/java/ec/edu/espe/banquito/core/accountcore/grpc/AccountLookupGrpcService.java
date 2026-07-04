package ec.edu.espe.banquito.core.accountcore.grpc;

import ec.edu.espe.banquito.core.accountcore.exception.AccountNotFoundException;
import ec.edu.espe.banquito.core.accountcore.grpc.accountlookup.AccountLookupResponse;
import ec.edu.espe.banquito.core.accountcore.grpc.accountlookup.AccountLookupServiceGrpc;
import ec.edu.espe.banquito.core.accountcore.grpc.accountlookup.GetAccountByNumberRequest;
import ec.edu.espe.banquito.core.accountcore.model.Account;
import ec.edu.espe.banquito.core.accountcore.repository.AccountRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class AccountLookupGrpcService extends AccountLookupServiceGrpc.AccountLookupServiceImplBase {

    private final AccountRepository accountRepository;

    public AccountLookupGrpcService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void getAccountByNumber(GetAccountByNumberRequest request,
                                   StreamObserver<AccountLookupResponse> responseObserver) {
        if (request.getAccountNumber().isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Account number is required")
                    .asRuntimeException());
            return;
        }

        try {
            Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                    .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

            responseObserver.onNext(AccountLookupResponse.newBuilder()
                    .setAccountId(account.getId())
                    .setAccountNumber(account.getAccountNumber())
                    .setCustomerId(account.getCustomerId())
                    .setStatus(account.getStatus().name())
                    .build());
            responseObserver.onCompleted();
        } catch (AccountNotFoundException exception) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(exception.getMessage())
                    .asRuntimeException());
        }
    }

}
