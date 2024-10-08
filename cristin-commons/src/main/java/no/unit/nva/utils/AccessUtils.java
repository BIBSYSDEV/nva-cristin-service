package no.unit.nva.utils;

import no.unit.nva.commons.json.JsonUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.core.attempt.Try.attempt;

public class AccessUtils {

    public static final String ACCESS_TOKEN_CLAIMS_SCOPE_FIELD = "scope";
    public static final String ACCESS_TOKEN_CLAIMS_FIELD = "claims";
    public static final String AUTHORIZER_FIELD = "authorizer";
    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
        "User: {} does not have required access right: {}";
    private static final String COGNITO_CLIENT_APP_ID_KEY = "COGNITO_CLIENT_APP_ID";
    private static final String COGNITO_USER_POOL_ID_KEY = "COGNITO_USER_POOL_ID";
    private static final String BACKEND_CLIENT_ID_KEY = "BACKEND_CLIENT_ID";
    private static final String BACKEND_CLIENT_ID_SECRET_KEY = "BACKEND_CLIENT_ID_SECRET";


    private static final Logger logger = LoggerFactory.getLogger(AccessUtils.class);
    public static final String GRANT_TYPE_PAYLOAD = "grant_type=client_credentials";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String BASIC = "Basic ";
    public static final String COGNITO_AUTHENTICATION_DOMAIN = "COGNITO_AUTHENTICATION_DOMAIN";
    public static final String COGNITO_TOKEN_ENDPOINT = "oauth2/token";
    public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String DOING_AUTHORIZED_REQUEST = "Client is doing authorized request";
    public static final String NO_USERNAME_FOUND = "NO USERNAME FOUND";

    /**
     * Validate if Requester is authorized to use IdentificationNumber to access a user.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user is not authorized to access a user with IdentificationNumber
     */
    public static void validateIdentificationNumberAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (!requesterIsUserAdministrator(requestInfo)) {
            var username = attempt(requestInfo::getUserName).orElse(fail -> NO_USERNAME_FOUND);
            logger.info(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT, username, MANAGE_OWN_AFFILIATION);
            throw new ForbiddenException();
        }
    }

    /**
     * Checks if the requester is permitted to act as a user administrator, either by having that specific role, or if
     * the client is internal backend.
     *
     * @param requestInfo information from request used to verify allowed permissions
     * @return true if user administrator otherwise false
     */
    public static boolean requesterIsUserAdministrator(RequestInfo requestInfo) {
        return requestInfo.userIsAuthorized(MANAGE_OWN_AFFILIATION) || requestInfo.clientIsInternalBackend();
    }

    public static boolean clientIsCustomerAdministrator(RequestInfo requestInfo) {
        return requestInfo.userIsAuthorized(MANAGE_CUSTOMERS);
    }

    /**
     * Fetches an internal backend token from Cognito.
     */
    public static String getBackendAccessToken() throws IOException, InterruptedException {

        try (var client = createHttpClient()) {
            var response = client.send(createTokenRequest(), ofString(StandardCharsets.UTF_8));
            var jsonTree = JsonUtils.dtoObjectMapper.readTree(response.body());
            return jsonTree.get(ACCESS_TOKEN).textValue();
        }
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    private static HttpRequest createTokenRequest() {
        return newBuilder(getCognitoTokenUrl())
                .POST(HttpRequest.BodyPublishers.ofString(GRANT_TYPE_PAYLOAD))
                .header(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED)
                .header(AUTHORIZATION, basicAuthHeader())
                .build();
    }

    private static URI getCognitoTokenUrl() {
        String cognitoAuthenticationDomain = new Environment().readEnv(COGNITO_AUTHENTICATION_DOMAIN);
        return UriWrapper.fromHost(cognitoAuthenticationDomain).addChild(COGNITO_TOKEN_ENDPOINT).getUri();
    }

    private static String basicAuthHeader() {
        var clientAppId = getBackendClientAppId();
        var clientAppSecret = getBackendClientAppSecret();
        return BASIC + Base64.getEncoder().encodeToString((clientAppId + ":" + clientAppSecret).getBytes());
    }

    private static String getBackendClientAppSecret() {
        return new Environment().readEnv(BACKEND_CLIENT_ID_SECRET_KEY);
    }

    private static String getBackendClientAppId() {
        return new Environment().readEnv(BACKEND_CLIENT_ID_KEY);
    }

    public static String getTestClientAppId() {
        return new Environment().readEnv(COGNITO_CLIENT_APP_ID_KEY);
    }

    public static String getUserPoolId() {
        return new Environment().readEnv(COGNITO_USER_POOL_ID_KEY);
    }
}
