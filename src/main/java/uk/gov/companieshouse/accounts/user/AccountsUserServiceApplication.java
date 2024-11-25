package uk.gov.companieshouse.accounts.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.accounts.user.util.StaticPropertyUtil;

@SpringBootApplication
public class AccountsUserServiceApplication {

    StaticPropertyUtil staticPropertyUtil;

    @Autowired
    public AccountsUserServiceApplication( final StaticPropertyUtil staticPropertyUtil ) {
        this.staticPropertyUtil = staticPropertyUtil;
    }

    public static void main(String[] args) {
        SpringApplication.run(AccountsUserServiceApplication.class, args);
    }

}