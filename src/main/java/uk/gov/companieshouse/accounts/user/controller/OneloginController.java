package uk.gov.companieshouse.accounts.user.controller;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.NotFoundRuntimeException;
import uk.gov.companieshouse.accounts.user.service.UsersService;
import uk.gov.companieshouse.api.accounts.user.api.OneloginInterface;
import uk.gov.companieshouse.api.accounts.user.model.UnlinkOneLogin;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
public class OneloginController implements OneloginInterface {

    private final UsersService usersService;

    private static final Logger LOG = LoggerFactory.getLogger( APPLICATION_NAMESPACE );

    private static final String PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN = "Please check the request and try again";

    public OneloginController( final UsersService usersService ) {
        this.usersService = usersService;
    }

    @Override
    public ResponseEntity<User> updateUserDetails( final String targetUserId, final String requestingUserId, final UnlinkOneLogin unlinkOneLogin, final String xRequestId ){
        LOG.infoContext( xRequestId, "Received request to update user details.", null );

        if ( Objects.isNull( unlinkOneLogin ) || Objects.isNull( unlinkOneLogin.getUnlinkOneLogin() ) ){
            LOG.errorContext( xRequestId, new IllegalArgumentException( "Request body was empty." ), null );
            throw new BadRequestRuntimeException( PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN );
        }

        if ( !usersService.userExists( requestingUserId ) ) {
            LOG.errorContext( xRequestId, new NullPointerException( String.format( "%s user does not exist", requestingUserId ) ), null );
            throw new NotFoundRuntimeException( APPLICATION_NAMESPACE, PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN );
        }

        var targetUser =
        usersService.fetchUser( targetUserId )
                .orElseThrow( () -> {
                    LOG.errorContext( xRequestId, new NullPointerException( String.format( "%s user does not exist", targetUserId ) ), null );
                    return new NotFoundRuntimeException( APPLICATION_NAMESPACE, PLEASE_CHECK_THE_REQUEST_AND_TRY_AGAIN );
                } );

        if ( unlinkOneLogin.getUnlinkOneLogin() && targetUser.getHasLinkedOneLogin() ){
            targetUser = usersService.unlinkOnelogin( targetUserId, requestingUserId );
        }

        return new ResponseEntity<>( targetUser, HttpStatus.OK );
    }

}
