package uk.gov.companieshouse.accounts.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Mapper(componentModel = "spring")
public interface UsersDtoDaoMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(target = "hasLinkedOneLogin",
            expression = "java(java.util.Objects.nonNull(users.getOneLoginData()))")
    @Mapping(source = "privateBetaUser", target = "isPrivateBetaUser")

    User daoToDto(Users users);

    @Mapping(source = "userId", target = "id")
    Users dtoToDao(User user);


}
