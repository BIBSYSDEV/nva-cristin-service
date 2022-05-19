package no.unit.nva.utils;

import no.unit.nva.commons.json.JsonUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static java.net.http.HttpRequest.newBuilder;
import static nva.commons.core.attempt.Try.attempt;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";
    public static final String EDIT_OWN_INSTITUTION_PROJECTS = "EDIT_OWN_INSTITUTION_PROJECTS";
    public static final String ACCESS_TOKEN_CLAIMS_SCOPE_FIELD = "scope";
    public static final String ACCESS_TOKEN_CLAIMS_FIELD = "claims";
    public static final String AUTHORIZER_FIELD = "authorizer";
    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
            "User:{} does not have required access right:{}";

    private static final String COGNITO_CLIENT_APP_ID_KEY = "COGNITO_CLIENT_APP_ID";
    private static final String COGNITO_USER_POOL_ID_KEY = "COGNITO_USER_POOL_ID";
    private static final String BACKEND_CLIENT_ID_KEY = "BACKEND_CLIENT_ID";
    private static final String BACKEND_CLIENT_ID_SECRET_KEY = "BACKEND_CLIENT_ID_SECRET";


    private static final Logger logger = LoggerFactory.getLogger(AccessUtils.class);

    /**
     * Validate if Requester is authorized to use IdentificationNumber to a access a user.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user is not authorized to access a user with IdentificationNumber
     */
    public static void validateIdentificationNumberAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToUseNationalIdentificationNumber(requestInfo)) {
            String nvaUsername = attempt(requestInfo::getNvaUsername).orElse(fail -> null);
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT,
                    nvaUsername, EDIT_OWN_INSTITUTION_USERS);
            throw new ForbiddenException();
        }
    }

    /**
     * Validate if Requester is authorized to create or change a project.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user has no access to create or change projects
     */
    public static void verifyRequesterCanEditProjects(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToEditProjects(requestInfo)) {
            String nvaUsername = attempt(requestInfo::getNvaUsername).orElse(fail -> null);
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT, nvaUsername, EDIT_OWN_INSTITUTION_PROJECTS);
            throw new ForbiddenException();
        }
    }

    public static boolean requesterIsUserAdministrator(RequestInfo requestInfo) {
        return requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_USERS)
               || requestInfo.clientIsInternalBackend();
    }

    private static boolean requesterHasNoAccessRightToUseNationalIdentificationNumber(RequestInfo requestInfo) {

        return !(
            requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_USERS)
            || requestInfo.clientIsInternalBackend()
        );
    }

    private static boolean requesterHasNoAccessRightToEditProjects(RequestInfo requestInfo) {
        return !requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_PROJECTS);
    }

    public static String getBackendAccessToken() throws IOException, InterruptedException {
        var cognitoTokenUrl = URI.create("https://nva-dev.auth.eu-west-1.amazoncognito.com/oauth2/token");
        var payload = "grant_type=client_credentials";

        var request = newBuilder(cognitoTokenUrl)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", basicAuthHeader())
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        var jsonTree = JsonUtils.dtoObjectMapper.readTree(response.body());
        return jsonTree.get("access_token").textValue();
    }

    private static String basicAuthHeader() {
        var clientAppId = getBackendClientAppId();
        var clientAppSecret = getBackendClientAppSecret();
        return "Basic " + Base64.getEncoder().encodeToString((clientAppId + ":" + clientAppSecret).getBytes());
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
