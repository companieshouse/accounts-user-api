package uk.gov.companieshouse.accounts.user.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.service.RolesService;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.RolesInterface;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.Roles;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class RolesController implements RolesInterface {

    private final RolesService rolesService;
    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public static final String PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN = "Please check the request and try again";

    public RolesController(RolesService rolesService, UsersService usersService) {
        this.rolesService = rolesService;
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<Void> addRole(@Valid Role role, @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {
        
        LOG.info( String.format( "%s: Attempting to add the new role '%s'", xRequestId, role.getId()));

        if(Objects.isNull(role.getId()) || Objects.isNull(role.getPermissions()) 
        || role.getId().isEmpty()|| role.getPermissions().isEmpty()){

            LOG.error(String.format("%s: An incomplete role was provided.", xRequestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        boolean roleAdded = rolesService.addRole(role);
 
        if (roleAdded) {
            LOG.debug(String.format("%s: Successfully added role %s",
            xRequestId,
            role.getId()));
            return new ResponseEntity<>(HttpStatus.CREATED);                    
        } else {
            LOG.debug( String.format( "%s: Unable to add the new role",xRequestId, role.getId()) );
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST );        
        }            
    }

    @Override
    public ResponseEntity<Void> deleteRole(@Pattern(regexp = "[0-9A-Za-z-_]{1,256}") String roleId,
            @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {
       
        LOG.info(String.format( "%s: Attempting to delete the '%s' role", xRequestId, roleId));

        if(Objects.isNull(roleId)){
            LOG.error(String.format("%s: A role_id was not provided.", xRequestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        boolean roleDeleted = rolesService.deleteRole(roleId);

        if (roleDeleted) {
            LOG.debug(String.format("%s: Successfully deleted the role '%s'",
            xRequestId,
            roleId));
    
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);                    
        } else {
            LOG.debug( String.format( "%s: Unable to delete the role '%s'",xRequestId, roleId));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST );        
        }   
    }

    @Override
    public ResponseEntity<Void> editRole(@Pattern(regexp = "[0-9A-Za-z-_]{1,256}") String roleId, @Valid PermissionsList updatedPermissions,
            @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {
        LOG.info(String.format( "%s: Attempting to update the permissions for the role '%s'", xRequestId, roleId));

        if(Objects.isNull(updatedPermissions) || updatedPermissions.isEmpty()){
            LOG.error(String.format("%s: A new set of permissions were not provided.", xRequestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        boolean roleUpdated = rolesService.editRole(roleId, updatedPermissions);

        if (roleUpdated) {
            LOG.debug(String.format("%s: Successfully updated the role '%s'",
            xRequestId,
            roleId));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT );                    
        } else {
            LOG.debug( String.format( "%s: Unable to update the role '%s'", xRequestId,roleId) );
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST );        
        }
    }

    @Override
    public ResponseEntity<Roles> getRoles(@Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {
        LOG.trace(String.format( "%s: Attempting to get all roles", xRequestId));

        Roles  roles = rolesService.getRoles();

        if (! roles.isEmpty() ) {

            LOG.debug(String.format("%s: Successfully fetched %d roles",
            xRequestId,
            roles.size()));
            
            return new ResponseEntity<>( roles, HttpStatus.OK );            
            
        } else {
            LOG.debug( String.format( "%s: Unable to get any roles",xRequestId) );
            return new ResponseEntity<>(new Roles(), HttpStatus.BAD_REQUEST);

        }
    }

    @Override
    public ResponseEntity<User> getUserRecord(@Pattern(regexp = "^[a-zA-Z0-9_-]*$") String userId,
            @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") String xRequestId) {

        LOG.debug( String.format( "%s: Attempting to search for the details of user: %s", xRequestId, userId ) );
        final var userOptional = usersService.fetchUser( userId );        
        if ( userOptional.isEmpty() ){
            LOG.debug( String.format( "%s: Could not find user: %s", xRequestId, userId ) );
            return new ResponseEntity<>( HttpStatus.NOT_FOUND );
        }
        final var user = userOptional.get();
        LOG.debug( String.format( "%s: Successfully fetched user: %s", xRequestId, userId ) );
        return new ResponseEntity<>( user, HttpStatus.OK );
    }
}
