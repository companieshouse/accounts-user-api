package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;
import uk.gov.companieshouse.api.accounts.user.model.Role;

@WritingConverter
public class MongoRoleWritingConverter implements Converter<Role, String> {

    @Override
    public String convert(@NonNull final Role role ) {
        return role.getValue();
    }

}

