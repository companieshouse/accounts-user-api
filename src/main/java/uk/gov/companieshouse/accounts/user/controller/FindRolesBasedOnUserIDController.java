package uk.gov.companieshouse.accounts.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.AccountsUserServiceApplication;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.UserRolesInterface;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
public class FindRolesBasedOnUserIDController implements UserRolesInterface {

    private static final Logger LOG = LoggerFactory.getLogger(AccountsUserServiceApplication.applicationNameSpace);

    private final UsersService userRolesService;

    public FindRolesBasedOnUserIDController( UsersService userRolesService) {
        this.userRolesService = userRolesService;
    }

    @Override
    public ResponseEntity<List<Role>> getUserRoles (final String requestId, final String userId){
        LOG.debug(String.format("%s: Retrieving user roles for user (%s) ...", requestId, userId));

        if (userId == null) {
            LOG.error(String.format("%s: No userId was provided.", requestId));
            throw new BadRequestRuntimeException("Please check the request and try again");
        }

        final var userRoles = userRolesService.findRolesByUserId(userId);
        if (userRoles.isEmpty()) {
            LOG.debug(String.format("%s: Unable to find roles for the userId: %s", requestId, userId));
            return new ResponseEntity<>(new ArrayList<>(userRoles), HttpStatus.NO_CONTENT);
        }

        LOG.debug(String.format("%s: Successfully retrieved roles for the userId: %s", requestId, userId));
        return new ResponseEntity<>(new ArrayList<>(userRoles), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> setUserRoles(@NotNull String s, @Pattern(regexp = "^[a-zA-Z0-9]*$") String s1, @Valid List<@Valid Role> list) {
        return null;
    }


}
