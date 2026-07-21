package ec.edu.espe.banquito.core.accountcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import ec.edu.espe.banquito.core.accountcore.dto.OffUsPaymentMessage;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ClearingPublisher {

    private static final ZoneId BANK_ZONE = ZoneId.of("America/Guayaquil");

    private final ObjectMapper objectMapper;
    private final String routingKey;
    private final Publisher publisher;

    public ClearingPublisher(ObjectMapper objectMapper,
                             @Value("${pubsub.project-id}") String projectId,
                             @Value("${pubsub.topic.clearing-events}") String topic,
                             @Value("${pubsub.routing-key.clearing-outbound:clearing.outbound}") String routingKey)
            throws Exception {
        this.objectMapper = objectMapper;
        this.routingKey = routingKey;
        this.publisher = Publisher.newBuilder(ProjectTopicName.of(projectId, topic)).build();
    }

    @PreDestroy
    public void shutdown() throws Exception {
        publisher.shutdown();
        publisher.awaitTermination(10, TimeUnit.SECONDS);
    }

    public void publishExternalTransfer(String originAccountNumber, String externalBankCode,
                                        String externalAccountNumber, BigDecimal amount,
                                        String concept) {
        OffUsPaymentMessage message = new OffUsPaymentMessage();
        message.setBatchId(UUID.randomUUID());
        message.setTransactionId(UUID.randomUUID());
        message.setRoutingCode(externalBankCode);
        message.setOriginAccount(originAccountNumber);
        message.setDestinationAccount(externalAccountNumber);
        message.setAmount(amount);
        message.setCurrency("USD");
        message.setConcept(concept);
        message.setValueDate(LocalDate.now(BANK_ZONE));

        publish(message);
    }

    private void publish(OffUsPaymentMessage message) {
        try {
            byte[] payload = objectMapper.writeValueAsBytes(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFrom(payload))
                    .putAttributes("routingKey", routingKey)
                    .putAttributes("source", "account-core-service")
                    .build();
            ApiFuture<String> messageId = publisher.publish(pubsubMessage);
            messageId.get();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo publicar evento de clearing en Pub/Sub", e);
        }
    }
}
