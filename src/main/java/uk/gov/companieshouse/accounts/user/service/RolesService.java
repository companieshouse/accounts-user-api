package uk.gov.companieshouse.accounts.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.models.UserRoles;
import uk.gov.companieshouse.accounts.user.repositories.UserRolesRepository;

@Transactional
@Service
public class RolesService {

    private final UserRolesRepository userRolesRepository;


    @Value("${database.limit:50}")
    private int limit;

    public RolesService(UserRolesRepository userRolesRepository) {
        this.userRolesRepository = userRolesRepository;
    }


    public List<UserRoles> getRoles(){
        return userRolesRepository.findAll();
    }

    public void addRole(){}

    public void deleteRole(){}

    public void editRole(){}

}
