package uk.gov.companieshouse.accounts.user.interceptor;


import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith( MockitoExtension.class )
@Tag( "unit-test" )
class AdminUserRolePermissionInterceptorTest {

    private AdminUserRolePermissionInterceptor interceptor;

    @BeforeEach
    void setup(){
        interceptor = new AdminUserRolePermissionInterceptor();
    }

    static Stream<Arguments> prehandleTestData(){
        return Stream.of(
                Arguments.of( "GET", "/admin/user/search", true ),
                Arguments.of( "GET", "/admin/user/unlinkonelogin", false ),
                Arguments.of( "PATCH", "/admin/user/search", false ),
                Arguments.of( "PATCH", "/admin/user/unlinkonelogin", true ),
                Arguments.of( "POST" , "/admin/payments-bulk-refunds", true )
        );
    }

    @ParameterizedTest
    @MethodSource( "prehandleTestData" )
    void preHandleDelegatesCorrectly( final String method, final String permissions, final boolean expectedResult ) throws IOException {
        final var request = new MockHttpServletRequest();
        request.setMethod( method );
        request.addHeader( "ERIC-Identity", "WITU004");
        request.addHeader( "ERIC-Identity-Type", "oauth2");
        request.addHeader( "ERIC-Authorised-Roles", permissions );

        final var response = new MockHttpServletResponse();

        Assertions.assertEquals( expectedResult, interceptor.preHandle( request, response, null ) );
    }

}
