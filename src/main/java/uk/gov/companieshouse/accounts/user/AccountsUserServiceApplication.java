package uk.gov.companieshouse.accounts.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountsUserServiceApplication {

    @Value("${spring.application.name}")
    public static String applicationNameSpace;

    public static void main(String[] args) {
        SpringApplication.run(AccountsUserServiceApplication.class, args);
    }

}