package ec.edu.espe.banquito.accountcore.client;

import ec.edu.espe.banquito.accountcore.dto.AccountingEntryReqDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AccountingServiceClient {

    private final RestClient restClient;

    public AccountingServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${accounting.service.url:http://localhost:8082}") String accountingServiceUrl) {
        this.restClient = restClientBuilder.baseUrl(accountingServiceUrl).build();
    }

    public void registerEntry(AccountingEntryReqDTO request) {
        restClient.post()
                .uri("/api/v2/accounting/entries")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
