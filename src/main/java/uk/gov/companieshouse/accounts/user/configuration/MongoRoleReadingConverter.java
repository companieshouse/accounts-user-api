package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.accounts.user.model.Role;

@ReadingConverter
public class MongoRoleReadingConverter implements Converter<String, Role> {

    @Override
    public Role convert( String role ) {
        return Role.fromValue( role );
    }

}

