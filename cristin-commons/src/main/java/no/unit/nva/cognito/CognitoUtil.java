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

public class CognitoUtil {


    public static final String CUSTOM_FEIDE_ID_ATTRIBUTE = "custom:feideId";
    public static final String CUSTOM_AFFILIATION_ATTRIBUTE = "custom:affiliation";
    public static final String CUSTOM_APPLICATION_ATTRIBUTE = "custom:application";
    public static final String FEIDE_AFFILIATION = "feide:[member, employee, staff]";
    public static final String NVA_APPLICATION = "NVA";
    public static final String PROBLEM_CREATING_USER_MESSAGE = "Problem creating user {}, {}";
    private static final Logger logger = LoggerFactory.getLogger(CognitoUtil.class);
    private final transient String userPoolId;
    private final transient String clientAppId;
    private final transient String region;
    private final AWSCognitoIdentityProvider cognitoIdentityProvider;



    /**
     * Create a CognitoUtil to help create an id_token to access services in API-gateway.
     *
     * @param userPoolId  id of the userpool
     * @param clientAppId clientapp secret from cognito
     * @param region      AWS region userpool is created in
     */
    public CognitoUtil(String userPoolId, String clientAppId, String region) {
        this.userPoolId = userPoolId;
        this.clientAppId = clientAppId;
        this.region = region;
        cognitoIdentityProvider = getCognitoIdentityProvider();
    }

    /**
     * Create user in the user to the user pool.
     *
     * @param feideId     User name for the sign up
     * @param password    Password for the sign up
     * @return username in cognito for user created from parameters.
     */
    public String createUser(String feideId, String password) {


        AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest();
        createUserRequest.setUserPoolId(getPoolId());
        createUserRequest.setUsername(feideId);
        createUserRequest.withTemporaryPassword(password);
        List<AttributeType> list = new ArrayList<>();

        list.add(new AttributeType().withName(CUSTOM_FEIDE_ID_ATTRIBUTE).withValue(feideId));
        list.add(new AttributeType().withName(CUSTOM_AFFILIATION_ATTRIBUTE).withValue(FEIDE_AFFILIATION));
        list.add(new AttributeType().withName(CUSTOM_APPLICATION_ATTRIBUTE).withValue(NVA_APPLICATION));

        createUserRequest.setUserAttributes(list);
        createUserRequest.setMessageAction(MessageActionType.SUPPRESS);

        try {
            AdminCreateUserResult result = cognitoIdentityProvider.adminCreateUser(createUserRequest);
            AdminSetUserPasswordRequest adminSetUserPasswordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(getPoolId())
                    .withUsername(feideId)
                    .withPassword(password)
                    .withPermanent(true);
            cognitoIdentityProvider.adminSetUserPassword(adminSetUserPasswordRequest);
            logger.warn("user created username={}, feideId={}", result.getUser().getUsername(), feideId);
            return result.getUser().getUsername();
        } catch (Exception e) {
            logger.warn(PROBLEM_CREATING_USER_MESSAGE, feideId, e.getMessage());
            return null;
        }
    }


    /**
     * Create a user using default values.
     *
     * @return id_token from loginUser()
     * @param feideId
     * @param password
     */
    public String loginUser(String feideId, String password) {

        return  loginUserAndReturnToken(feideId, password).orElse(null);
    }


    /**
     * Delete the user with feideId from Cognito Userpool.
     *
     * @return true if operation is a success
     * @param feideId
     */
    public void deleteUser(String feideId) {
        getUsername(feideId).ifPresent(username -> {
            try {
                deleteUserCognito(username);
            } catch (Exception e) {
                logger.warn("Error deleting user:{}, {}", username, e.getMessage());
            }
        });
    }

    /**
     * Delete a user in Cognito userpool.
     *
     * @param feideId string identifying user in userpool
     * @return If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body
     * If not successful a runtime exception is thrown
     */
    private AdminDeleteUserResult deleteUserCognito(String feideId) {
        AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                .withUserPoolId(getPoolId())
                .withUsername(feideId);
        return getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
    }

    private Optional<String> getUsername() {
        return getUsername();
    }

    private Optional<String> getUsername(String feideId) {
        Optional<String> username = Optional.empty();
        try {
            ListUsersRequest listUsersRequest = new ListUsersRequest()
                    .withUserPoolId(getPoolId())
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
     * @return result of operation containing credentials and tokens
     */
    private AdminInitiateAuthResult loginUserCognito(String username, String password) {

        final Map<String, String> authParams = Map.of("USERNAME", username, "PASSWORD", password);
        final AdminInitiateAuthRequest initiateAuthRequest = new AdminInitiateAuthRequest()
                .withUserPoolId(getPoolId())
                .withClientId(getClientId())
                .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .withAuthParameters(authParams);
        return cognitoIdentityProvider.adminInitiateAuth(initiateAuthRequest);
    }

    private Optional<String> loginUserAndReturnToken(String username, String password) {
        Optional<String> token = Optional.empty();
        try {
            token = Optional.of(loginUserCognito(username, password).getAuthenticationResult().getIdToken());
        } catch (Exception e) {
            logger.warn("Error loginUserAndReturnToken username:{}, {}", username, e.getMessage());
        }
        return token;
    }


    private AWSCognitoIdentityProvider getCognitoIdentityProvider() {
        AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        return AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(region))
                .build();
    }

    private String getPoolId() {
        return userPoolId;
    }

    private String getClientId() {
        return clientAppId;
    }


}