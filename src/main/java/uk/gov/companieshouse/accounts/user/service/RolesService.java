package uk.gov.companieshouse.accounts.user.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.mapper.RolesDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.UserRole;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.Roles;

@Transactional
@Service
public class RolesService {

    private final RolesRepository userRolesRepository;

    private final RolesDtoDaoMapper rolesDtoDaoMapper;


    public RolesService(RolesRepository userRolesRepository, RolesDtoDaoMapper rolesDtoDaoMapper) {
        this.userRolesRepository = userRolesRepository;
        this.rolesDtoDaoMapper = rolesDtoDaoMapper;
    }

    public Roles getRoles(){
    
       List<Role> rolesFromDatabase = userRolesRepository
                            .findAll()
                            .stream()
                            .map(rolesDtoDaoMapper::daoToDto)
                            .toList();

        Roles roles = new Roles();
        roles.addAll(rolesFromDatabase);
        return roles;
    }

    public boolean addRole(final Role role){ 
        UserRole userRole = rolesDtoDaoMapper.dtoToDao(role);

        boolean success = false;
        if (! userRolesRepository.existsById(userRole.getId())){
            userRolesRepository.insert(userRole);
            if (userRolesRepository.existsById(userRole.getId())){
                success=true;
            }
        }
        return success;
    }

    public boolean deleteRole(final String roleId){
        boolean success = false;
        
        if (userRolesRepository.existsById(roleId)){
            userRolesRepository.deleteById(roleId);
            if (! userRolesRepository.existsById(roleId)){
                success = true;
            }
        }
        return success;
    }

    public int editRole(final String roleId, final PermissionsList permissions){
        if (userRolesRepository.existsById(roleId)){
            final var permissionsSet = new HashSet<>( permissions );
            final var update = new Update().set( "permissions", permissionsSet );
            return userRolesRepository.updateRole( roleId, update);
        }
        return 0;
    }
}
