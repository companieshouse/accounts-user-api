package uk.gov.companieshouse.accounts.user.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    public ResponseEntity<List<Role>> getUserRoles(@NotNull String s, @Pattern(regexp = "^[a-zA-Z0-9]*$") String s1) {
        return null;
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
