package uk.gov.companieshouse.accounts.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.FindUserBasedOnEmailInterface;
import uk.gov.companieshouse.api.accounts.user.model.UsersList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.List;
import java.util.Objects;

@RestController
public class FindUserBasedOnEmailController implements FindUserBasedOnEmailInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    public FindUserBasedOnEmailController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<UsersList> searchUserDetails( final String xRequestId, final List<String> emails ) {

        if(Objects.isNull(emails) || emails.isEmpty()){
            LOG.error(String.format("%s: No emails were provided.", xRequestId));
            throw new BadRequestRuntimeException("Please check the request and try again");
        }

        LOG.debug( String.format( "%s: Attempting to search for the details of these users: %s",
                xRequestId, String.join(", ", emails) ) );

        final var users = new UsersList();
        users.addAll( usersService.fetchUsers( emails ) );

        if ( users.isEmpty() ) {
            LOG.debug( String.format( "%s: Unable to find any of these users: %s",
                    xRequestId, String.join(", ", emails) ) );
            return new ResponseEntity<>( users, HttpStatus.NO_CONTENT);
        }

        LOG.debug( String.format( "%s: Successfully fetched users: %s",
                xRequestId, String.join(", ", emails) ) );


        return new ResponseEntity<>( users, HttpStatus.OK );
    }

}
