package uk.gov.companieshouse.accounts.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Component
@Mapper( componentModel = "spring" )
public interface UsersDtoDaoMapper {

    @Mappings({ @Mapping( source = "id", target = "userId" ) })
    User daoToDto( Users users );

    @Mappings({ @Mapping( source = "userId", target = "id" ) })
    Users dtoToDao( User user );

    Role daoToDto(Role role);

    Role dtoToDao(Role role);

}
