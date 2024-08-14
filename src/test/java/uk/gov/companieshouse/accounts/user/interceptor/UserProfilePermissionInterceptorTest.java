package uk.gov.companieshouse.accounts.user.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.repositories.OauthRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfilePermissionInterceptorTest {

    @InjectMocks
    private UserProfilePermissionInterceptor interceptor;

    @Mock
    private OauthRepository oauthRepository;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("preHandle Authorised")
    void preHandleAuthorised() throws Exception {
        Oauth2AuthorisationsDao oauthAuthorisation = new Oauth2AuthorisationsDao();
        Map<String, String> tokenPermissions = new HashMap<>(Map.of("user_profile", "read"));
        oauthAuthorisation.setTokenPermissions(tokenPermissions);

        when(oauthRepository.findByToken("12345")).thenReturn(oauthAuthorisation);

        request.addHeader("Authorization", "Bearer " + "12345");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(oauthAuthorisation, request.getAttribute("oauth2_authorisation"));
    }

    @Test
    @DisplayName("preHandle Unauthorised Missing authorisations")
    void preHandleUnAuthorisedMissingAuthorisations() throws Exception {
        when(oauthRepository.findByToken("12345")).thenReturn(null);

        request.addHeader("Authorization", "Bearer " + "12345");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
}
