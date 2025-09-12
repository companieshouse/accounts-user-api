package uk.gov.companieshouse.accounts.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.accounts.user.mapper.AdminPermissionsDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.AdminPermissions;
import uk.gov.companieshouse.accounts.user.repositories.AdminPermissionsRepository;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroup;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroups;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import org.springframework.data.mongodb.core.query.Update;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;


@Transactional
@Service
public class AdminPermissionsService {

    private final AdminPermissionsRepository adminPermissionsRepository;

    private final AdminPermissionsDtoDaoMapper adminPermissionsDtoDaoMapper;

    public AdminPermissionsService(AdminPermissionsRepository adminPermissionsRepository, AdminPermissionsDtoDaoMapper adminPermissionsDtoDaoMapper) {
        this.adminPermissionsRepository = adminPermissionsRepository;
        this.adminPermissionsDtoDaoMapper = adminPermissionsDtoDaoMapper;
    }

    public AdminPermissionsGroups getAdminGroup(){

       List<AdminPermissionsGroup> adminPermissions = adminPermissionsRepository
               .findAll()
               .stream()
               .map(adminPermissionsDtoDaoMapper::daoToDto).toList();

        AdminPermissionsGroups adminPermissionsGroups = new AdminPermissionsGroups();
        adminPermissionsGroups.addAll(adminPermissions);
        return adminPermissionsGroups;
    }

    public AdminPermissionsGroup addAdminPermissions(final AdminPermissionsGroup adminPermissionsGroup){
        AdminPermissions adminPermissions = adminPermissionsDtoDaoMapper.dtoToDao(adminPermissionsGroup);
        if(adminPermissions.getId() == null) {
            adminPermissions.setId(UUID.randomUUID().toString());
        }
        if (adminPermissionsRepository.findByEntraGroupId(adminPermissions.getEntraGroupId()) == null){
            var newAdminPermissions = adminPermissionsRepository.insert(adminPermissions);
            if (newAdminPermissions.getEntraGroupId() != null){
                return adminPermissionsDtoDaoMapper.daoToDto(newAdminPermissions);
            }
        }
        return null;
    }

    public boolean deleteAdminPermissions(final String adminPermissionsGroupId){
        boolean success = false;

        if (adminPermissionsRepository.existsById(adminPermissionsGroupId)){
            adminPermissionsRepository.deleteById(adminPermissionsGroupId);
            if (! adminPermissionsRepository.existsById(adminPermissionsGroupId)){
                success = true;
            }
        }
        return success;
    }

    public boolean editAdminPermissions(final String adminPermissionsGroupId, final PermissionsList permissions){
        boolean success = false;
        if (adminPermissionsRepository.existsById(adminPermissionsGroupId)){
            final var permissionsSet = new HashSet<>( permissions );
            final var update = new Update().set( "permissions", permissionsSet );
            success = adminPermissionsRepository.updateRole( adminPermissionsGroupId, update) == 1;
        }
        return success;
    }
}
