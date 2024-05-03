package uk.gov.companieshouse.accounts.user.service;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.exceptions.BadRequestRuntimeException;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.RolesRepository;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.RolesList;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Transactional
@Service
public class UsersService {

    private final UsersRepository usersRepository;

    private final RolesRepository userRolesRepository;
    private final UsersDtoDaoMapper usersDtoDaoMapper;

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
}
