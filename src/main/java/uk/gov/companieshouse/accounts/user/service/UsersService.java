package uk.gov.companieshouse.accounts.user.service;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.Users;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final UsersDtoDaoMapper usersDtoDaoMapper;

    public UsersService(UsersRepository usersRepository, UsersDtoDaoMapper usersDtoDaoMapper) {
        this.usersRepository = usersRepository;
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

    public int setRoles( String userId, List<Role> roles ){
        final var rolesSet = new HashSet<>( roles );
        final var update = new Update().set( "roles", rolesSet );
        return usersRepository.updateUser( userId, update );
    }

}
