package uk.gov.companieshouse.accounts.user.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import uk.gov.companieshouse.api.filter.CustomCorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebSecurityConfig {

    /**
     * Configure Http Security.
     */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new CustomCorsFilter(externalMethods()), CsrfFilter.class);

        return http.build();
    }

    @Bean
    public List<String> externalMethods() {
        return Arrays.asList(HttpMethod.GET.name());
    }
}
