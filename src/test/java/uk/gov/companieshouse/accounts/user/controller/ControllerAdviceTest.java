package uk.gov.companieshouse.accounts.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.companieshouse.accounts.user.configuration.InterceptorConfig;
import uk.gov.companieshouse.accounts.user.exceptions.InternalServerErrorRuntimeException;
import uk.gov.companieshouse.accounts.user.exceptions.NotFoundRuntimeException;
import uk.gov.companieshouse.accounts.user.repositories.OauthRepository;
import uk.gov.companieshouse.accounts.user.service.UsersService;

@Tag("unit-test")
@WebMvcTest( FindUserBasedOnEmailController.class )
@AutoConfigureMockMvc(addFilters = false)
class ControllerAdviceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;

    @MockBean
    InterceptorConfig interceptorConfig;

    @MockBean
    OauthRepository oauthRepository;

    @BeforeEach
    void setup() {
        Mockito.doNothing().when(interceptorConfig).addInterceptors( any() );
    }

    @Test
    void testNotFoundRuntimeError() throws Exception {
        Mockito.doThrow(new NotFoundRuntimeException("accounts-user-api", "Couldn't find users"))
                .when( usersService ).fetchUsers( any() );

        mockMvc.perform(get( "/users/search?user_email=jessica.simpson@hollywood.com" )
                        .header("X-Request-Id", "theId123") )
                .andExpect(status().isNotFound());
    }

    @Test
    void testBadRequestRuntimeError() throws Exception {
        mockMvc.perform( get( "/users/search" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
    }

    @Test
    void testConstraintViolationError() throws Exception {
        mockMvc.perform( get( "/users/search?user_email=abc" ).header("X-Request-Id", "theId123") ).andExpect(status().isBadRequest());
    }

    @Test
    void testOnInternalServerError() throws Exception {
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"))
                .when( usersService ).fetchUsers( any() );

        mockMvc.perform(get( "/users/search?user_email=jessica.simpson@hollywood.com" )
                        .header("X-Request-Id", "theId123") )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testOnInternalServerErrorRuntimeException() throws Exception {
        Mockito.doThrow(new InternalServerErrorRuntimeException("Internal Server Error"))
                .when( usersService ).fetchUsers( any() );

        mockMvc.perform(get( "/users/search?user_email=jessica.simpson@hollywood.com" )
                        .header("X-Request-Id", "theId123") )
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testNotFoundExceptionWhenUserNotFoundForRoles() throws Exception {
        mockMvc.perform( get( "/users/123dd/roles" ).header("X-Request-Id", "theId123") ).andExpect(status().isNotFound());
    }

}
