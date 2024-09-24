package no.unit.nva.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.cognito.CognitoUtil;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.*;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.utils.AccessUtils.BASIC;

public final class UserUtils {

    private static final Logger logger = LoggerFactory.getLogger(UserUtils.class);
    public static final String RESPONSE_FROM_USER_CREATE = "Response from user create={}";
    public static final String USER_CREATED_WITH_ROLES = "User {} created with role(s) '{}'";

    private UserUtils() {
        // NO-OP
    }

    /**
     * Creates user in Cognito used for Karate tests.
     */
    public static void createUserWithRoles(String username, String password, String nationalIdentityNumber,
                                           URI customerId, Set<String> roles) throws IOException, InterruptedException {

        final String poolId = AccessUtils.getUserPoolId();
        final String appClientClientId = AccessUtils.getTestClientAppId();

        createNvaUserWithRoles(nationalIdentityNumber, customerId, roles);

        CognitoUtil.deleteUser(username, poolId);
        CognitoUtil.adminCreateUser(username, password, nationalIdentityNumber, poolId, appClientClientId);

        logger.info(USER_CREATED_WITH_ROLES, username, roles);

    }

    private static void createNvaUserWithRoles(String nationalIdentityNumber, URI customerId, Set<String> roles)
        throws IOException, InterruptedException {
        try (var client = createHttpClient()) {
            final var response = client.send(createHttpRequest(nationalIdentityNumber, customerId, roles),
                    BodyHandlers.ofString(StandardCharsets.UTF_8));
            logger.info(RESPONSE_FROM_USER_CREATE, response);
        }
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    private static HttpRequest createHttpRequest(String nationalIdentityNumber,
                                                 URI customerId,
                                                 Set<String> roles) throws IOException, InterruptedException {
        final var userRoles = new UserRoles(nationalIdentityNumber, customerId, roles);
        final var body = JsonUtils.dtoObjectMapper.writeValueAsString(userRoles);
        return newBuilder(createUserServiceUri())
                .header(AUTHORIZATION, BASIC + AccessUtils.getBackendAccessToken())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }


    private static URI createUserServiceUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild("users-roles").addChild("users").getUri();
    }


    @SuppressWarnings("unused")
    private static class UserRoles {

        private static final String NATIONAL_IDENTITY_NUMBER_FIELD = "nationalIdentityNumber";
        private static final String CUSTOMER_ID_FIELD = "customerId";
        private static final String ROLES_FIELD = "roles";


        @JsonProperty(NATIONAL_IDENTITY_NUMBER_FIELD)
        public String nationalIdentityNumber;
        @JsonProperty(CUSTOMER_ID_FIELD)
        public URI customerId;
        @JsonProperty(ROLES_FIELD)
        public Set<RoleDto> roles;

        public UserRoles(String nationalIdentityNumber, URI customerId, Set<String> roles) {
            this.nationalIdentityNumber = nationalIdentityNumber;
            this.customerId = customerId;
            this.roles = roles.stream().map(RoleDto::new).collect(Collectors.toSet());
        }

        public String getNationalIdentityNumber() {
            return nationalIdentityNumber;
        }

        public URI getCustomerId() {
            return customerId;
        }

        public Set<RoleDto> getRoles() {
            return roles;
        }

        public void setNationalIdentityNumber(String nationalIdentityNumber) {
            this.nationalIdentityNumber = nationalIdentityNumber;
        }

        public void setCustomerId(URI customerId) {
            this.customerId = customerId;
        }

        public void setRoles(Set<RoleDto> roles) {
            this.roles = roles;
        }
    }

    @SuppressWarnings("unused")
    private static class RoleDto {

        @JsonProperty("type")
        public static final String TYPE = "Role";


        @JsonProperty("rolename")
        public String rolename;

        public RoleDto(String rolename) {
            this.rolename = rolename;
        }

        public String getType() {
            return TYPE;
        }

        public void setRolename(String rolename) {
            this.rolename = rolename;
        }

        public String getRolename() {
            return rolename;
        }
    }
}
