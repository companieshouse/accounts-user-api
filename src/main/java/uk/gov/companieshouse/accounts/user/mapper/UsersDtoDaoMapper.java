package uk.gov.companieshouse.accounts.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Mapper(componentModel = "spring" , uses= DateTimeMapper.class)
public interface UsersDtoDaoMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "privateBetaUser", target = "isPrivateBetaUser")
    @Mapping(source = "created", target =   "created", qualifiedByName = "localDateTimeToOffsetDateTime")
    @Mapping(target = "hasLinkedOneLogin",
            expression = "java(java.util.Objects.nonNull(users.getOneLoginData()))")
    User daoToDto(Users users);

    @Mapping(source = "created", target =   "created", qualifiedByName = "offsetDateTimeToDateTime")
    @Mapping(source = "userId", target = "id")
    Users dtoToDao(User user);
}
