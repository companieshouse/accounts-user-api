package uk.gov.companieshouse.accounts.user.mapper;


import org.mapstruct.Mapper;

import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.api.accounts.user.model.Role;


@Mapper(componentModel = "spring")
public interface RolesDtoDaoMapper {

    Role daoToDto(UserRole userRoles);  
    UserRole dtoToDao(Role role);
}
