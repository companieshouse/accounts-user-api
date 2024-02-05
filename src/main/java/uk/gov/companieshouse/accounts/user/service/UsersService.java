package uk.gov.companieshouse.accounts.user.service;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.accounts.user.mapper.UsersDtoDaoMapper;
import uk.gov.companieshouse.accounts.user.repositories.UsersRepository;
import uk.gov.companieshouse.api.accounts.user.model.User;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final UsersDtoDaoMapper usersDtoDaoMapper;

    public UsersService(UsersRepository usersRepository, UsersDtoDaoMapper usersDtoDaoMapper) {
        this.usersRepository = usersRepository;
        this.usersDtoDaoMapper = usersDtoDaoMapper;
    }

    public List<User> fetchUsers( List<String> emails ) {
        return usersRepository.fetchUsers(emails)
                              .stream()
                              .map(usersDtoDaoMapper::daoToDto)
                              .toList();
    }

}
