package ec.edu.espe.banquito.accountcore.converter;

import ec.edu.espe.banquito.accountcore.enums.TransactionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, String> {

    @Override
    public String convertToDatabaseColumn(TransactionStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public TransactionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return switch (dbData) {
            case "COMPLETADA", "COMPLETED" -> TransactionStatus.COMPLETED;
            case "RECHAZADA", "REJECTED" -> TransactionStatus.REJECTED;
            default -> TransactionStatus.valueOf(dbData);
        };
    }
}
