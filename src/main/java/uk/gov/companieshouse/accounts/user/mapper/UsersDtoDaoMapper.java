package uk.gov.companieshouse.accounts.user.mapper;

import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import uk.gov.companieshouse.accounts.user.models.OneLoginDataDao;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Mapper(componentModel = "spring")
public interface UsersDtoDaoMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "privateBetaUser", target = "isPrivateBetaUser")
    @Mapping(source = "oneLoginData", target = "hasLinkedOneLogin", qualifiedByName = "isValidOneLoginData")
    User daoToDto(Users users);

    @Mapping(source = "userId", target = "id")
    Users dtoToDao(User user);

    @Named("isValidOneLoginData")
    default boolean isValidOneLoginData(OneLoginDataDao oneLoginData) {
        JSONObject jsonObject = new JSONObject(oneLoginData);

        if (jsonObject.has("oneLoginUserId")) {
            String userIdValue = jsonObject.getString("oneLoginUserId");
            return userIdValue != null && !userIdValue.isEmpty();
        } else {
            return false;
        }
    }


}
