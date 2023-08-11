package no.unit.nva.cognito;

import no.unit.nva.utils.AccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.MessageActionType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

public class CognitoUtil {

    public static final String NVA_NIN_ATTRIBUTE = "custom:feideIdNin";

    public static final String CURRENT_CUSTOMER_CLAIM_NAME = "custom:customerId";
    public static final URI CURRENT_CUSTOMER_CLAIM_VALUE =
            URI.create("https://api.dev.nva.aws.unit.no/customer/f50dff3a-e244-48c7-891d-cc4d75597321");

    public static final String ACCESS_RIGHTS_CLAIM_NAME = "custom:accessRights";
    public static final String MULTI_VALUE_DELIMITER = ",";
    public static final String AT = "@";
    public static final String ACCESS_RIGHTS_CLAIM_VALUE = constructAccessRights(CURRENT_CUSTOMER_CLAIM_VALUE);
    public static final String PROBLEM_CREATING_USER_MESSAGE = "Problem creating user {}, {}";
    public static final String REGION = "eu-west-1";
    public static final String ADMIN_TESTUSER_ID_KEY = "ADMIN_TESTUSER_ID";
    public static final String ADMIN_TESTUSER_PASSWORD_KEY = "ADMIN_TESTUSER_PASSWORD";
    public static final String ADMIN_TESTUSER_NIN_KEY = "ADMIN_TESTUSER_NIN";


    public static final String SIMPLE_TESTUSER_ID_KEY = "SIMPLE_TESTUSER_ID";
    public static final String SIMPLE_TESTUSER_PASSWORD_KEY = "SIMPLE_TESTUSER_PASSWORD";
    public static final String SIMPLE_TESTUSER_NIN_KEY = "SIMPLE_TESTUSER_NIN";

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtil.class);

    /**
     * Create user in the user to the user pool.
     *
     * @param username Username for the sign-up
     * @param password    Password for the sign-up
     * @param poolId      Identifier for Cognito user pool
     * @return username in cognito for user created from parameters.
     */
    public static String adminCreateUser(String username,
                                         String password,
                                         String nvaUserNin,
                                         String poolId,
                                         String clientId) {

        List<AttributeType> list = new ArrayList<>();
        list.add(newAttribute(CURRENT_CUSTOMER_CLAIM_NAME, CURRENT_CUSTOMER_CLAIM_VALUE.toString()));
        list.add(newAttribute(ACCESS_RIGHTS_CLAIM_NAME, ACCESS_RIGHTS_CLAIM_VALUE));
        list.add(newAttribute(NVA_NIN_ATTRIBUTE, nvaUserNin));

        var createUserRequest = AdminCreateUserRequest
            .builder()
            .userPoolId(poolId)
                .username(username)
            .userAttributes(list)
            .messageAction(MessageActionType.SUPPRESS)
            .build();

        try {
            var result = getCognitoIdentityProvider().adminCreateUser(createUserRequest);
            var adminSetUserPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(poolId)
                    .username(username)
                .password(password)
                .permanent(true)
                .build();
            getCognitoIdentityProvider().adminSetUserPassword(adminSetUserPasswordRequest);
            logger.debug("user created username={}", result.user().username());
            if (isNull(loginUser(username, password, clientId))) {
                logger.error("User cannot login after create");
                return null;
            }  // Force setting of user attributes in Cognito
            return result.user().username();
        } catch (Exception e) {
            logger.warn(PROBLEM_CREATING_USER_MESSAGE, username, e.getMessage());
            return null;
        }
    }

    /**
     * Login existing user and return token.
     *
     * @param cognitoUserName Username in cognito to use for login
     * @param password    users password
     * @param clientId    Cognito AppClientId
     * @return access_token from loginUser()
     */
    public static String loginUser(String cognitoUserName, String password, String clientId) {
        try {
            final var loginResult = loginUserCognito(cognitoUserName, password, clientId);
            logger.debug("LoginResult.getAuthenticationResult={}", loginResult.authenticationResult());
            return loginResult.authenticationResult().accessToken();
        } catch (Exception e) {
            logger.warn("Error loginUserAndReturnToken cognitoUsername:{}, {}", cognitoUserName, e.getMessage());
            return null;
        }
    }

    /**
     * Delete the user with nvaUsername from Cognito Userpool.
     *
     * @param userName Username to use for login
     * @param poolId      Cognito userpool id
     */
    public static void deleteUser(String userName, String poolId) {
        getUsername(userName, poolId).ifPresent(username -> {
            try {
                deleteUserCognito(username, poolId);
            } catch (Exception e) {
                logger.warn("Error deleting user:{}, {}", username, e.getMessage());
            }
        });
    }

    private static String constructAccessRights(URI currentCustomerClaimValue) {
        return Stream.of(AccessUtils.EDIT_OWN_INSTITUTION_USERS, AccessUtils.MANAGE_OWN_PROJECTS)
            .map(right -> right + AT + currentCustomerClaimValue.toString())
            .collect(Collectors.joining(MULTI_VALUE_DELIMITER));
    }

    private static AttributeType newAttribute(String atributeName, String attributeValue) {
        return AttributeType.builder().name(atributeName).value(attributeValue).build();
    }

    /**
     * Delete a user in Cognito userpool.
     *
     * @param username string identifying user in userpool
     * @param poolId      Cognito Userpool Id
     * @return If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body If not
     *     successful a runtime exception is thrown
     */
    private static AdminDeleteUserResponse deleteUserCognito(String username, String poolId) {
        var deleteUserRequest = AdminDeleteUserRequest.builder()
            .userPoolId(poolId)
                .username(username)
            .build();
        return getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
    }

    private static Optional<String> getUsername(String nvaUsername, String poolId) {
        try {
            var listUsersRequest = ListUsersRequest.builder()
                .userPoolId(poolId)
                    .filter(String.format("username = \"%s\"", nvaUsername))
                .build();
            return getCognitoIdentityProvider().listUsers(listUsersRequest).users().stream()
                .findFirst()
                .map(UserType::username);
        } catch (Exception e) {
            logger.warn("Error getting username:{}, {}", nvaUsername, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Sign the user in (login) to userpool.
     *
     * @param username username
     * @param password password
     * @param clientId Cognito AppClientId
     * @return result of operation containing credentials and tokens
     */
    private static InitiateAuthResponse loginUserCognito(String username,
                                                              String password,
                                                         String clientId
    ) {

        final Map<String, String> authParams = Map.of("USERNAME", username, "PASSWORD", password);

        final InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
            .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
            .authParameters(authParams)
            .build();
        return getCognitoIdentityProvider().initiateAuth(initiateAuthRequest);
    }

    private static CognitoIdentityProviderClient getCognitoIdentityProvider() {

        return CognitoIdentityProviderClient
            .builder()
                .httpClient(UrlConnectionHttpClient.create())
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.of(REGION))
            .build();
    }
}
