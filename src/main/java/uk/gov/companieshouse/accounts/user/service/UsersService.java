package uk.gov.companieshouse.accounts.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.*;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.models.Users;
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

    public List<User> fetchUsers( final List<String> emails ) {
        return Optional.ofNullable(usersRepository.fetchUsers(emails))
                .orElse(new ArrayList<>())
                              .stream()
                              .map(usersDtoDaoMapper::daoToDto)
                              .toList();
    }

    public Optional<User> fetchUser( final String userId ){
        return usersRepository.findUsersById( userId )
                              .map( usersDtoDaoMapper::daoToDto );
    }

    public Set<Role> findRolesByUserId(final String userId) {
        return  usersRepository.findRolesByUserId(userId).map(Users::getRoles)
                .orElse(Set.of());

    }


}
