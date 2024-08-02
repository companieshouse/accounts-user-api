package uk.gov.companieshouse.accounts.user.interceptor;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import uk.gov.companieshouse.accounts.user.models.Oauth2AuthorisationsDao;
import uk.gov.companieshouse.accounts.user.repositories.OauthRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

@ExtendWith(MockitoExtension.class)
class Oauth2AuthorizationInterceptorTest {

    @InjectMocks
    private Oauth2AuthorizationInterceptor interceptor;

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
    @DisplayName("preHandle requestMethod = options")
    void preHandleRequestMethodOptions() throws Exception {
        request.setMethod("OPTIONS");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    @DisplayName("preHandle no request authorization provided")
    void preHandleNoRequestAuthorization() throws Exception {
        request.addHeader("Authorization", "");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Access denied", response.getErrorMessage());
    }

    @Test
    @DisplayName("preHandle invalid basic authorization")
    void preHandleInvalidBasicAuthorization() throws Exception {
        request.addHeader("Authorization", "Basic invalid");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("invalid-authorization-header", response.getErrorMessage());
        var wwwAuthenticateHeader = "Basic error='invalid_literal' error-desc='Invalid username:password string' realm='https://identity.company-information.service.gov.uk'";
        assertEquals(wwwAuthenticateHeader, response.getHeader(WWW_AUTHENTICATE));
    }

    @Test
    @DisplayName("preHandle basic Authorised")
    void preHandleBasicAuthorised() throws Exception {
        Oauth2AuthorisationsDao mockOad = mock(Oauth2AuthorisationsDao.class);

        when(oauthRepository.findByToken("12345")).thenReturn(mockOad);
        when(mockOad.getTokenValidUntil()).thenReturn(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)+300);

        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic MTIzNDU2Nzg5MC5hcHBzLmNoLmdvdi51azo=");
        request.addParameter("access_token", "12345");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(mockOad, request.getAttribute("oauth2_authorisation"));
    }

    @Test
    @DisplayName("preHandle basic Authorise null token")
    void preHandleBasicAuthorisedNullToken() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic MTIzNDU2Nzg5MC5hcHBzLmNoLmdvdi51azo=");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("invalid-authorization-header", response.getErrorMessage());
        var wwwAuthenticateHeader = "Bearer error='invalid_token' error-desc='Invalid or no authorization header provided' realm='https://identity.company-information.service.gov.uk'";
        assertEquals(wwwAuthenticateHeader, response.getHeader(WWW_AUTHENTICATE));
    }

    @Test
    @DisplayName("preHandle bearer token Authorised")
    void preHandleBearerTokenAuthorised() throws Exception {
        Oauth2AuthorisationsDao mockOad = mock(Oauth2AuthorisationsDao.class);

        when(oauthRepository.findByToken("12345")).thenReturn(mockOad);
        when(mockOad.getTokenValidUntil()).thenReturn(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)+300);

        request.addHeader("Authorization", "Bearer " + "12345");

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(mockOad, request.getAttribute("oauth2_authorisation"));
    }

    @Test
    @DisplayName("preHandle Unauthorised Missing authorisations")
    void preHandleUnAuthorisedMissingAuthorisations() throws Exception {
        when(oauthRepository.findByToken("12345")).thenReturn(null);

        request.addHeader("Authorization", "Bearer " + "12345");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    @DisplayName("preHandle Unauthorised No Read Permission")
    void preHandleUnAuthorisedNoReadPermission() throws Exception {
        Oauth2AuthorisationsDao oauthAuthorisation = new Oauth2AuthorisationsDao();
        Map<String, String> tokenPermissions = new HashMap<>(Map.of("user_profile", "update"));
        oauthAuthorisation.setTokenPermissions(tokenPermissions);

        when(oauthRepository.findByToken("12345")).thenReturn(oauthAuthorisation);

        request.addHeader("Authorization", "Bearer " + "12345");

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}
