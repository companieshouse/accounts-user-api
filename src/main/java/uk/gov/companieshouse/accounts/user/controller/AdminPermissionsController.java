package uk.gov.companieshouse.accounts.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.service.AdminPermissionsService;
import uk.gov.companieshouse.api.accounts.user.api.AdminPermissionsInterface;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroup;
import uk.gov.companieshouse.api.accounts.user.model.AdminPermissionsGroups;
import uk.gov.companieshouse.api.accounts.user.model.PermissionsList;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.util.Objects;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;


@RestController
public class AdminPermissionsController implements AdminPermissionsInterface {

    private final AdminPermissionsService adminPermissionsService;

    private static final Logger LOG = LoggerFactory.getLogger(APPLICATION_NAMESPACE);

    public static final String PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN = "Please check the request and try again";

    public AdminPermissionsController(AdminPermissionsService adminPermissionsService) {
        this.adminPermissionsService = adminPermissionsService;
    }

    @Override
    public ResponseEntity<AdminPermissionsGroups> getAdminPermissions(@Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {
        LOG.trace(String.format( "%s: Attempting to get all roles", xRequestId));

        AdminPermissionsGroups  roles = adminPermissionsService.getAdminGroup();

        if (! roles.isEmpty() ) {

            LOG.debug(String.format("%s: Successfully fetched %d roles",
                    xRequestId,
                    roles.size()));

            return new ResponseEntity<>( roles, HttpStatus.OK );

        } else {
            LOG.debug( String.format( "%s: Unable to get any roles",xRequestId) );
            return new ResponseEntity<>(new AdminPermissionsGroups(), HttpStatus.BAD_REQUEST);

        }
    }

@Override
    public ResponseEntity<AdminPermissionsGroup> addAdminPermission(@Valid AdminPermissionsGroup adminPermissionsGroup, @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {

        LOG.info(String.format("%s: Attempting to add the new role '%s'", xRequestId, adminPermissionsGroup.getEntraGroupId()));

        if (Objects.isNull(adminPermissionsGroup.getEntraGroupId()) || adminPermissionsGroup.getEntraGroupId().isEmpty()) {

            LOG.error(String.format("%s: An incomplete role was provided.", xRequestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        var adminPermissionsAdded = adminPermissionsService.addAdminPermissions(adminPermissionsGroup);

        if (adminPermissionsAdded != null) {
            LOG.debug(String.format("%s: Successfully added role %s",
                    xRequestId,
                    adminPermissionsGroup.getEntraGroupId()));
            return new ResponseEntity<>(adminPermissionsAdded, HttpStatus.CREATED);
        } else {
            LOG.debug(String.format("%s: Unable to add the new role %s", xRequestId, adminPermissionsGroup.getEntraGroupId()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Void> editAdminPermission(@Pattern(regexp = "[0-9A-Za-z-_]{1,256}") String roleId, @Valid PermissionsList updatedPermissions,
                                                    @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {
        LOG.info(String.format("%s: Attempting to update the permissions for the role '%s'", xRequestId, roleId));

        boolean roleUpdated = adminPermissionsService.editAdminPermissions(roleId, updatedPermissions);

        if (roleUpdated) {
            LOG.debug(String.format("%s: Successfully updated the role '%s'",
                    xRequestId,
                    roleId));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            LOG.debug(String.format("%s: Unable to update the role '%s'", xRequestId, roleId));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Void> deleteAdminPermission(@Pattern(regexp = "[0-9A-Za-z-_]{1,256}") String roleId,
                                                      @Pattern(regexp = "[0-9A-Za-z-_]{8,32}") final String xRequestId) {

        LOG.info(String.format("%s: Attempting to delete the '%s' role", xRequestId, roleId));

        if (Objects.isNull(roleId)) {
            LOG.error(String.format("%s: A role_id was not provided.", xRequestId));
            throw new BadRequestRuntimeException(PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN);
        }

        boolean roleDeleted = adminPermissionsService.deleteAdminPermissions(roleId);

        if (roleDeleted) {
            LOG.debug(String.format("%s: Successfully deleted the role '%s'",
                    xRequestId,
                    roleId));

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            LOG.debug(String.format("%s: Unable to delete the role '%s'", xRequestId, roleId));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}