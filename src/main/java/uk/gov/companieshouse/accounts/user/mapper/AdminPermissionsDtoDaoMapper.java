package uk.gov.companieshouse.accounts.user.mapper;


import org.mapstruct.Mapper;
import uk.gov.companieshouse.accounts.user.models.AdminPermissions;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroup;


@Mapper(componentModel = "spring")
public interface AdminPermissionsDtoDaoMapper {

    AdminPermissionsGroup daoToDto(AdminPermissions adminPermissionsGroup);

    AdminPermissions dtoToDao(AdminPermissionsGroup adminPermissions);
}
