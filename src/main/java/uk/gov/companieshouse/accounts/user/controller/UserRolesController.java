package uk.gov.companieshouse.accounts.user.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.NotFoundRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.UserRolesInterface;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class UserRolesController implements UserRolesInterface {

    public static final String PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN = "Please check the request and try again";
    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public UserRolesController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<RolesList> getUserRoles (final String userId, final String requestId){
        LOG.debug(String.format("%s: Retrieving user roles for user (%s) ...", requestId, userId));

        if (Objects.isNull(userId)){
            LOG.error(String.format("%s: No userId was provided.", requestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }
        final var userRolesOptional = usersService.fetchUser(userId).map(User::getRoles);
        if (userRolesOptional.isEmpty()) {
            LOG.debug(String.format("%s: Unable to find roles for the userId: %s", requestId, userId));
            return new ResponseEntity<>(new RolesList(), HttpStatus.NO_CONTENT);
        }
        final var userRoles= userRolesOptional.get();
        LOG.debug(String.format("%s: Successfully retrieved roles for the userId: %s", requestId, userId));

        return new ResponseEntity<>( userRoles, HttpStatus.OK);
    }


    @Override
    public ResponseEntity<Void> setUserRoles( final String userId, final RolesList roles, final String xRequestId ) {

        if( Objects.isNull(roles) || roles.isEmpty() ){
            LOG.error(String.format("%s: No roles were provided.", xRequestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        LOG.debug( String.format( "%s: attempting to set the status of %s to %s",
                xRequestId, userId, roles) );

        if ( usersService.fetchUser( userId ).isEmpty() ){
            LOG.debug( String.format( "%s: Unable to find user: %s", xRequestId, userId ) );
            throw new NotFoundRuntimeException( "accounts-user-api", PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        final var numUpdatedUsers = usersService.setRoles( userId, roles );
        LOG.trace( String.format( "%s: Ran update query for userId %s, resulted in %d record%s being updated.", xRequestId, userId, numUpdatedUsers, numUpdatedUsers == 1 ? "" : "s"  ) );


        LOG.debug( String.format( "%s: Successfully set status of %s to %s", xRequestId, userId,  roles) );

        return new ResponseEntity<>(HttpStatus.OK );
    }

}
