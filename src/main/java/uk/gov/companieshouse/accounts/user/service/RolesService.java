package uk.gov.companieshouse.accounts.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.models.UserRoles;
import uk.gov.companieshouse.accounts.user.repositories.UserRolesRepository;

@Transactional
@Service
public class RolesService {

    private final UserRolesRepository userRolesRepository;

    public RolesService(UserRolesRepository userRolesRepository) {
        this.userRolesRepository = userRolesRepository;
    }

    public List<UserRoles> getRoles(){
        return userRolesRepository.findAll();
    }

    public void addRole(final UserRoles role){ 
        if (! userRolesRepository.existsById(role.getId())){
            userRolesRepository.insert(role);
        }
    }

    public void deleteRole(final UserRoles role){
        if (userRolesRepository.existsById(role.getId())){
            userRolesRepository.deleteById(role.getId());
        }
    }

    public void editRole(final UserRoles userRoles){

        List<String> permissionsSet = new ArrayList<>();
        permissionsSet.addAll(userRoles.getPermissions());

        final var update = new Update().set( "permissions", permissionsSet );
        userRolesRepository.updateRole( userRoles.getId(), update );

    }
}
