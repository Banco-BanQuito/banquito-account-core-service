package ec.edu.espe.banquito.accountcore.service;

import ec.edu.espe.banquito.accountcore.dto.OffUsPaymentMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ClearingPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String clearingQueue;

    public ClearingPublisher(RabbitTemplate rabbitTemplate,
                             @Value("${app.rabbitmq.clearing-queue:clearing.outbound.queue}") String clearingQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.clearingQueue = clearingQueue;
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
        message.setValueDate(LocalDate.now());

        rabbitTemplate.convertAndSend(clearingQueue, message);
    }
}
