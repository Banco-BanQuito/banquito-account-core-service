package ec.edu.espe.banquito.accountcore.converter;

import ec.edu.espe.banquito.accountcore.enums.AccountType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public AccountType convertToEntityAttribute(String dbData) {
        return AccountType.fromDatabaseValue(dbData);
    }
}
