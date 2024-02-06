package uk.gov.companieshouse.accounts.user.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.FindUserBasedOnEmailInterface;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class FindUserBasedOnEmailController implements FindUserBasedOnEmailInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public FindUserBasedOnEmailController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<List<User>> searchUserDetails( final String xRequestId, List<String> emails ) {

        emails = Optional.ofNullable( emails ).orElse( List.of() );

        LOG.debug( String.format( "%s: Attempting to search for the details of these users: %s", xRequestId, String.join(", ", emails) ) );

        if ( emails.isEmpty() ) {
            LOG.error( String.format( "%s: No emails were provided.", xRequestId ) );
            throw new BadRequestRuntimeException("Please check the request and try again");
        }

        final var users = usersService.fetchUsers( emails );

        if ( users.isEmpty() ) {
            LOG.debug( String.format( "%s: Unable to find any of these users: %s", xRequestId, String.join(", ", emails) ) );
            return new ResponseEntity<>(users, HttpStatus.NO_CONTENT);
        }

        LOG.debug( String.format( "%s: Successfully fetched users: %s", xRequestId, String.join(", ", emails) ) );

        // TODO: Team Phoenix will introduced hasLinkedOneLogin. Until then, will set value to null.
        users.forEach( user -> user.setHasLinkedOneLogin( null ) );

        return new ResponseEntity<>( users, HttpStatus.OK );
    }

}
