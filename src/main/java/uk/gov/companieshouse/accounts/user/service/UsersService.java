package uk.gov.companieshouse.accounts.user.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.Role;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final UsersDtoDaoMapper usersDtoDaoMapper;

    public UsersService(UsersRepository usersRepository, UsersDtoDaoMapper usersDtoDaoMapper) {
        this.usersRepository = usersRepository;
        this.usersDtoDaoMapper = usersDtoDaoMapper;
    }

    @Transactional
    public List<User> fetchUsers( final List<String> emails ) {
        return Optional.ofNullable(usersRepository.fetchUsers(emails))
                .orElse(new ArrayList<>())
                              .stream()
                              .map(usersDtoDaoMapper::daoToDto)
                              .toList();
    }

    @Transactional
    public Optional<User> fetchUser( final String userId ){
        return usersRepository.findUsersById( userId )
                              .map( usersDtoDaoMapper::daoToDto );
    }

    @Transactional
    public void setRoles( String userId, List<Role> roles ){
        final var update = new Update().set( "roles", new HashSet<>( roles ) );
        usersRepository.updateUser( userId, update );
    }

}
