package uk.gov.companieshouse.accounts.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.GetUserRecordInterface;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class GetUserRecordController implements GetUserRecordInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public GetUserRecordController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<User> getUserDetails( final String userId, final String xRequestId) {

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
