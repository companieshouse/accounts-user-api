package uk.gov.companieshouse.accounts.user.service;

import static uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil.APPLICATION_NAMESPACE;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Transactional
@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final RolesRepository userRolesRepository;
    private final UsersDtoDaoMapper usersDtoDaoMapper;

    private static final Logger LOG = LoggerFactory.getLogger( APPLICATION_NAMESPACE );

    @Value("${database.limit:50}")
    private int limit;

    public UsersService(UsersRepository usersRepository, RolesRepository userRolesRepository, UsersDtoDaoMapper usersDtoDaoMapper) {
        this.usersRepository = usersRepository;
        this.userRolesRepository = userRolesRepository;
        this.usersDtoDaoMapper = usersDtoDaoMapper;
    }

    public List<User> fetchUsers( final List<String> emails ) {


        return Objects.requireNonNullElse(usersRepository.fetchUsers(emails), new ArrayList<Users>())
                              .stream()
                              .map(usersDtoDaoMapper::daoToDto)
                .collect(Collectors.toList());
    }

    public Optional<User> fetchUser( final String userId ){
        return usersRepository.findUsersById( userId )
                              .map( usersDtoDaoMapper::daoToDto );
    }

    public boolean userExists( final String userId ){
        return usersRepository.existsById(userId);
    }

    public int setRoles( final String userId, final RolesList roles ){

        List<String> errors = new ArrayList<>();
        roles.forEach(role ->{
            if(!userRolesRepository.existsById(role)){
               errors.add(role);
            }
        });
        if(!errors.isEmpty()){
            throw new BadRequestRuntimeException(String.format("%s not valid role(s)",Strings.join(errors, ',')));
        }
        final var rolesSet = new HashSet<>( roles );
        final var update = new Update().set( "roles", rolesSet );
        return usersRepository.updateUser( userId, update );
    }

    public List<User> fetchUsersUsingPartialEmail(final String partialEmail) {

        List<Users> foundUsers = usersRepository.findUsersByEmailLike(partialEmail, Limit.of(limit));
                    
        return Objects.requireNonNullElse(foundUsers, new ArrayList<Users>())
                .stream()
                .map(usersDtoDaoMapper::daoToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public User unlinkOnelogin( final String targetUserId, final String unlinkedByUserId ){
        if ( Objects.isNull( targetUserId ) || Objects.isNull( unlinkedByUserId ) ){
            LOG.error( "targetUserId and unlinkedByUserId cannot be null" );
            throw new IllegalArgumentException( "targetUserId or unlinkedByUserId were null" );
        }

        final var update = new Update()
                .unset( "one_login_data" )
                .set( "one_login_link_removed_by", unlinkedByUserId )
                .set( "one_login_link_removed_at", LocalDateTime.now() );

        final var numRecordsUpdated = usersRepository.updateUser( targetUserId, update );
        if ( numRecordsUpdated == 0 ){
            LOG.error( String.format( "Failed to update user %s", targetUserId ) );
            throw new InternalServerErrorRuntimeException( "Failed to update user record" );
        }

        return fetchUser( targetUserId ).orElseThrow( () -> {
            LOG.error( String.format( "Updated user %s, but was unable to retrieve updated user record", targetUserId ) );
            return new InternalServerErrorRuntimeException( "Updated user %s, but was unable to retrieve updated user record" );
        } );
    }

}
