package no.unit.nva.cognito;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.MessageActionType;
import com.amazonaws.services.cognitoidp.model.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

public class CognitoUtil {

    public static final String CUSTOM_FEIDE_ID_ATTRIBUTE = "custom:feideId";
    public static final String CUSTOM_AFFILIATION_ATTRIBUTE = "custom:affiliation";
    public static final String CUSTOM_APPLICATION_ATTRIBUTE = "custom:application";
    public static final String CUSTOM_APPLICATION_ROLES_ATTRIBUTE = "custom:applicationRoles";
    public static final String TEST_USER_ROLES_ADMIN_AND_CREATOR = "Creator,Institution-admin";
    public static final String FEIDE_AFFILIATION = "[member, employee, staff]";
    public static final String NVA_APPLICATION = "NVA";
    public static final String PROBLEM_CREATING_USER_MESSAGE = "Problem creating user {}, {}";
    public static final String REGION = "eu-west-1";
    public static final String TESTUSER_FEIDE_ID_KEY = "TESTUSER_FEIDE_ID";
    public static final String TESTUSER_PASSWORD_KEY = "TESTUSER_PASSWORD";
    public static final String SIMPLE_TESTUSER_FEIDE_ID_KEY = "SIMPLE_TESTUSER_FEIDE_ID";
    public static final String SIMPLE_TESTUSER_PASSWORD_KEY = "SIMPLE_TESTUSER_PASSWORD";
    public static final String COGNITO_CLIENT_APP_ID_KEY = "COGNITO_CLIENT_APP_ID";
    public static final String COGNITO_USER_POOL_ID_KEY = "COGNITO_USER_POOL_ID";

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtil.class);


    /**
     * Create user in the user to the user pool.
     *
     * @param feideId  User name for the sign up
     * @param password Password for the sign up
     * @param poolId   Identifier for Cognito userpool
     * @return username in cognito for user created from parameters.
     */
    public static String createUser(String feideId, String password, String poolId, String clientId) {

        AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest();
        createUserRequest.setUserPoolId(poolId);
        createUserRequest.setUsername(feideId);
        List<AttributeType> list = new ArrayList<>();

        list.add(new AttributeType().withName(CUSTOM_FEIDE_ID_ATTRIBUTE).withValue(feideId));
        list.add(new AttributeType().withName(CUSTOM_AFFILIATION_ATTRIBUTE).withValue(FEIDE_AFFILIATION));
        list.add(new AttributeType().withName(CUSTOM_APPLICATION_ATTRIBUTE).withValue(NVA_APPLICATION));
        list.add(new AttributeType().withName(CUSTOM_APPLICATION_ROLES_ATTRIBUTE)
                .withValue(TEST_USER_ROLES_ADMIN_AND_CREATOR));


        createUserRequest.setUserAttributes(list);
        createUserRequest.setMessageAction(MessageActionType.SUPPRESS);

        try {
            AdminCreateUserResult result = getCognitoIdentityProvider().adminCreateUser(createUserRequest);
            AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(poolId)
                    .withUsername(feideId)
                    .withPassword(password)
                    .withPermanent(true);
            getCognitoIdentityProvider().adminSetUserPassword(adminSetUserPasswordRequest);
            logger.debug("user created username={}, feideId={}", result.getUser().getUsername(), feideId);
            if (isNull(loginUser(feideId, password, poolId, clientId))) {
                logger.error("User cannot login after create");
                return null;
            }  // Force setting of user attributes in Cognito
            return result.getUser().getUsername();
        } catch (Exception e) {
            logger.warn(PROBLEM_CREATING_USER_MESSAGE, feideId, e.getMessage());
            return null;
        }
    }


    /**
     * Create a user using given values.
     *
     * @param feideId  Username to use for login
     * @param password users password
     * @param poolId   Cognito userpoolid
     * @param clientId Cognito AppClientId
     * @return id_token from loginUser()
     */
    public static String loginUser(String feideId, String password, String poolId, String clientId) {
        try {
            final AdminInitiateAuthResult loginResult = loginUserCognito(feideId, password, poolId, clientId);
            logger.debug("LoginResult.getAuthenticationResult={}", loginResult.getAuthenticationResult());
            return loginResult.getAuthenticationResult().getIdToken();
        } catch (Exception e) {
            logger.warn("Error loginUserAndReturnToken username:{}, {}", feideId, e.getMessage());
            return null;
        }
    }

    /**
     * Delete the user with feideId from Cognito Userpool.
     *
     * @param feideId Username to use for login
     * @param poolId  Cognito userpool id
     */
    public static void deleteUser(String feideId, String poolId) {
        getUsername(feideId, poolId).ifPresent(username -> {
            try {
                deleteUserCognito(username, poolId);
            } catch (Exception e) {
                logger.warn("Error deleting user:{}, {}", username, e.getMessage());
            }
        });
    }

    /**
     * Delete a user in Cognito userpool.
     *
     * @param feideId string identifying user in userpool
     * @param poolId  Cognito Userpool Id
     * @return If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body
     *         If not successful a runtime exception is thrown
     */
    private static AdminDeleteUserResult deleteUserCognito(String feideId, String poolId) {
        AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                .withUserPoolId(poolId)
                .withUsername(feideId);
        return getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
    }

    private static Optional<String> getUsername(String feideId, String poolId) {
        Optional<String> username = Optional.empty();
        try {
            ListUsersRequest listUsersRequest = new ListUsersRequest()
                    .withUserPoolId(poolId)
                    .withFilter(String.format("email = \"%s\"", feideId));
            username = getCognitoIdentityProvider().listUsers(listUsersRequest).getUsers().stream()
                    .findFirst()
                    .map(UserType::getUsername);
        } catch (Exception e) {
            logger.warn("Error getting username:{}, {}", feideId, e.getMessage());
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
    private static AdminInitiateAuthResult loginUserCognito(String username,
                                                            String password,
                                                            String poolId,
                                                            String clientId) {

        final Map<String, String> authParams = Map.of("USERNAME", username, "PASSWORD", password);
        final AdminInitiateAuthRequest initiateAuthRequest = new AdminInitiateAuthRequest()
                .withUserPoolId(poolId)
                .withClientId(clientId)
                .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .withAuthParameters(authParams);
        return getCognitoIdentityProvider().adminInitiateAuth(initiateAuthRequest);
    }


    private static AWSCognitoIdentityProvider getCognitoIdentityProvider() {
        AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        return AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(REGION))
                .build();
    }
}
