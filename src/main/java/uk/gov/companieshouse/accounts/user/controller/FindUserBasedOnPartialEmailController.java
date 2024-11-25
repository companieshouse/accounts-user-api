package uk.gov.companieshouse.accounts.user.controller;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.FindUsersBasedOnAPartialEmailInterface;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.api.accounts.user.model.UsersList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class FindUserBasedOnPartialEmailController implements FindUsersBasedOnAPartialEmailInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public FindUserBasedOnPartialEmailController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<UsersList> searchUsersDetailsUsingPartialEmail( final String xRequestId, final String partialEmail ) {

        if(Objects.isNull(partialEmail) || partialEmail.isEmpty()){
            LOG.error(String.format("%s: No partial email was provided.", xRequestId));
            throw new BadRequestRuntimeException("Please check the request and try again");
        }

        LOG.debug( 
            String.format( "%s: Attempting to search for users with an email address containing: %s",   xRequestId, partialEmail)
        );

        final UsersList users = new UsersList();
        users.addAll(usersService.fetchUsersUsingPartialEmail(partialEmail));

        ResponseEntity<UsersList> response = new ResponseEntity<>( users, HttpStatus.NO_CONTENT);

        if (! users.isEmpty() ) {

            LOG.debug(String.format("%s: Successfully fetched %d users : %s",
            xRequestId,
            users.size(),
            users.stream().map(User::getEmail).collect(Collectors.joining(","))));
            
            response = new ResponseEntity<>( users, HttpStatus.OK );            
        } else {
            LOG.debug( String.format( "%s: Unable to find any of these users containing: %s",xRequestId, partialEmail) );
        }

        return response;
    }
}
