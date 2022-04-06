package no.unit.nva.cognito;

import static java.util.Objects.isNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.utils.AccessUtils;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

public class CognitoUtil {

    public static final String NVA_USERNAME_ATTRIBUTE = "custom:nvaUsername";
    public static final String CURRENT_CUSTOMER_CLAIM_NAME = "custom:customerId";
    public static final URI CURRENT_CUSTOMER_CLAIM_VALUE = URI.create("https://example.currentcustomer.org");
    public static final String ACCESS_RIGHTS_CLAIM_NAME = "custom:accessRights";
    public static final String MULTI_VALUE_DELIMITER = ",";
    public static final String AT = "@";
    public static final String ACCESS_RIGHTS_CLAIM_VALUE = constructAccessRights(CURRENT_CUSTOMER_CLAIM_VALUE);
    public static final String PROBLEM_CREATING_USER_MESSAGE = "Problem creating user {}, {}";
    public static final String REGION = "eu-west-1";
    public static final String TESTUSER_FEIDE_ID_KEY = "TESTUSER_FEIDE_ID";
    public static final String TESTUSER_PASSWORD_KEY = "TESTUSER_PASSWORD";
    public static final String SIMPLE_TESTUSER_FEIDE_ID_KEY = "SIMPLE_TESTUSER_FEIDE_ID";
    public static final String SIMPLE_TESTUSER_PASSWORD_KEY = "SIMPLE_TESTUSER_PASSWORD";
    public static final String COGNITO_CLIENT_APP_ID_KEY = "COGNITO_CLIENT_APP_ID";
    public static final String COGNITO_USER_POOL_ID_KEY = "COGNITO_USER_POOL_ID";
    private static final Logger logger = LoggerFactory.getLogger(CognitoUtil.class);

    public static String getCognitoUserPoolId() {
        return new Environment().readEnv(COGNITO_USER_POOL_ID_KEY);
    }

    public static String getCognitoAppClientId() {
        return new Environment().readEnv(COGNITO_CLIENT_APP_ID_KEY);
    }

    /**
     * Create user in the user to the user pool.
     *
     * @param nvaUsername User name for the sign up
     * @param password    Password for the sign up
     * @param poolId      Identifier for Cognito userpool
     * @return username in cognito for user created from parameters.
     */
    public static String createAdminUser(String nvaUsername, String password, String poolId, String clientId) {

        List<AttributeType> list = new ArrayList<>();
        list.add(newAttribute(nvaUsername, NVA_USERNAME_ATTRIBUTE));
        list.add(newAttribute(CURRENT_CUSTOMER_CLAIM_VALUE.toString(), CURRENT_CUSTOMER_CLAIM_NAME));
        list.add(newAttribute(ACCESS_RIGHTS_CLAIM_NAME, ACCESS_RIGHTS_CLAIM_VALUE));

        var createUserRequest = AdminCreateUserRequest
            .builder()
            .userPoolId(poolId)
            .username(nvaUsername)
            .userAttributes(list)
            .messageAction(MessageActionType.SUPPRESS)
            .build();

        try {
            var result = getCognitoIdentityProvider().adminCreateUser(createUserRequest);
            var adminSetUserPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(poolId)
                .username(nvaUsername)
                .password(password)
                .permanent(true)
                .build();
            getCognitoIdentityProvider().adminSetUserPassword(adminSetUserPasswordRequest);
            logger.debug("user created username={}, nvaUsername={}", result.user().username(), nvaUsername);
            if (isNull(loginUser(nvaUsername, password, poolId, clientId))) {
                logger.error("User cannot login after create");
                return null;
            }  // Force setting of user attributes in Cognito
            return result.user().username();
        } catch (Exception e) {
            logger.warn(PROBLEM_CREATING_USER_MESSAGE, nvaUsername, e.getMessage());
            return null;
        }
    }

    /**
     * Create a user using given values.
     *
     * @param nvaUsername Username to use for login
     * @param password    users password
     * @param poolId      Cognito userpoolid
     * @param clientId    Cognito AppClientId
     * @return id_token from loginUser()
     */
    public static String loginUser(String nvaUsername, String password, String poolId, String clientId) {
        try {
            final var loginResult = loginUserCognito(nvaUsername, password, poolId, clientId);
            logger.debug("LoginResult.getAuthenticationResult={}", loginResult.authenticationResult());
            return loginResult.authenticationResult().idToken();
        } catch (Exception e) {
            logger.warn("Error loginUserAndReturnToken username:{}, {}", nvaUsername, e.getMessage());
            return null;
        }
    }

    /**
     * Delete the user with nvaUsername from Cognito Userpool.
     *
     * @param nvaUsername Username to use for login
     * @param poolId      Cognito userpool id
     */
    public static void deleteUser(String nvaUsername, String poolId) {
        getUsername(nvaUsername, poolId).ifPresent(username -> {
            try {
                deleteUserCognito(username, poolId);
            } catch (Exception e) {
                logger.warn("Error deleting user:{}, {}", username, e.getMessage());
            }
        });
    }

    private static String constructAccessRights(URI currentCustomerClaimValue) {
        return Stream.of(AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS, AccessUtils.EDIT_OWN_INSTITUTION_USERS)
            .map(right -> right + AT + currentCustomerClaimValue.toString())
            .collect(Collectors.joining(MULTI_VALUE_DELIMITER));
    }

    private static AttributeType newAttribute(String nvaUsername, String nvaUsernameAttribute) {
        return AttributeType.builder().name(nvaUsernameAttribute).value(nvaUsername).build();
    }

    /**
     * Delete a user in Cognito userpool.
     *
     * @param nvaUsername string identifying user in userpool
     * @param poolId      Cognito Userpool Id
     * @return If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body If not
     *     successful a runtime exception is thrown
     */
    private static AdminDeleteUserResponse deleteUserCognito(String nvaUsername, String poolId) {
        var deleteUserRequest = AdminDeleteUserRequest.builder()
            .userPoolId(poolId)
            .username(nvaUsername)
            .build();
        return getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
    }

    private static Optional<String> getUsername(String nvaUsername, String poolId) {
        Optional<String> username = Optional.empty();
        try {
            var listUsersRequest = ListUsersRequest.builder()
                .userPoolId(poolId)
                .filter(String.format("email = \"%s\"", nvaUsername))
                .build();
            username = getCognitoIdentityProvider().listUsers(listUsersRequest).users().stream()
                .findFirst()
                .map(UserType::username);
        } catch (Exception e) {
            logger.warn("Error getting username:{}, {}", nvaUsername, e.getMessage());
        }
        return username;
    }

    /**
     * Sign the user in (login) to userpool.
     *
     * @param username username
     * @param password password
     * @param poolId   Cognito userpoo Id
     * @param clientId Cognito AppClientId
     * @return result of operation containing credentials and tokens
     */
    private static AdminInitiateAuthResponse loginUserCognito(String username,
                                                              String password,
                                                              String poolId,
                                                              String clientId) {

        final Map<String, String> authParams = Map.of("USERNAME", username, "PASSWORD", password);
        final var initiateAuthRequest = AdminInitiateAuthRequest.builder()
            .userPoolId(poolId)
            .clientId(clientId)
            .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
            .authParameters(authParams)
            .build();
        return getCognitoIdentityProvider().adminInitiateAuth(initiateAuthRequest);
    }

    private static CognitoIdentityProviderClient getCognitoIdentityProvider() {

        return CognitoIdentityProviderClient
            .builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.of(REGION))
            .build();
    }
}
