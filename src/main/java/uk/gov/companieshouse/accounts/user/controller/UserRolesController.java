package uk.gov.companieshouse.accounts.user.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.NotFoundRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.UserRolesInterface;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class UserRolesController implements UserRolesInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public UserRolesController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<List<Role>> getUserRoles (final String requestId, final String userId){
        LOG.debug(String.format("%s: Retrieving user roles for user (%s) ...", requestId, userId));

        if (Objects.isNull(userId)){
            LOG.error(String.format("%s: No userId was provided.", requestId));
            throw new BadRequestRuntimeException("Please check the request and try again");
        }
        final var userRolesOptional = usersService.fetchUser(userId).map(User::getRoles);
        if (userRolesOptional.isEmpty()) {
            LOG.debug(String.format("%s: Unable to find roles for the userId: %s", requestId, userId));
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NO_CONTENT);
        }
        final var userRoles= userRolesOptional.get();
        LOG.debug(String.format("%s: Successfully retrieved roles for the userId: %s", requestId, userId));
        return new ResponseEntity<>(new ArrayList<>(userRoles), HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Void> setUserRoles( final String xRequestId, final String userId, final List<Role> roles ) {

        if( Objects.isNull(roles) || roles.isEmpty() ){
            LOG.error(String.format("%s: No roles were provided.", xRequestId));
            throw new BadRequestRuntimeException("Please check the request and try again");
        }

        LOG.debug( String.format( "%s: attempting to set the status of %s to %s", xRequestId, userId, String.join( ",", roles.stream().map( Role::getValue ).toList() ) ) );

        if ( usersService.fetchUser( userId ).isEmpty() ){
            LOG.debug( String.format( "%s: Unable to find user: %s", xRequestId, userId ) );
            throw new NotFoundRuntimeException( "accounts-user-api", "Please check the request and try again" );
        }

        usersService.setRoles( userId, roles );

        LOG.debug( String.format( "%s: Successfully set status of %s to %s", xRequestId, userId, String.join( ",", roles.stream().map( Role::getValue ).toList() ) ) );

        return new ResponseEntity<>(HttpStatus.CREATED );
    }

}
