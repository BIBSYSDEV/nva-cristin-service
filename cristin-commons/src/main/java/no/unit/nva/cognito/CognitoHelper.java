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
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.MessageActionType;
import com.amazonaws.services.cognitoidp.model.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static nva.commons.core.attempt.Try.attempt;

/**
 * The CognitoHelper class abstracts the functionality of connecting to the Cognito user pool and Federated Identities.
 */
public class CognitoHelper {

    public static final String CUSTOM_FEIDE_ID_ATTRIBUTE = "custom:feideId";
    public static final String CUSTOM_AFFILIATION_ATTRIBUTE = "custom:affiliation";
    public static final String CUSTOM_APPLICATION_ATTRIBUTE = "custom:application";
    public static final String CUSTOM_APPLICATION_ROLES_ATTRIBUTE = "custom:applicationRoles";
    public static final String CUSTOM_ACCESS_RIGHTS_ATTRIBUTE = "custom:accessRights";
    public static final String INSTITUTION_ADMIN_ROLE = "Institution-admin";
    public static final String FEIDE_AFFILIATION = "feide:[member, employee, staff]";
    public static final String NVA_APPLICATION = "NVA";
    public static final String PROBLEM_CREATING_USER_MESSAGE = "Problem creating user {}, {}";
    private static final Logger logger = LoggerFactory.getLogger(CognitoHelper.class);
    private final transient String userPoolId;
    private final transient String clientAppId;
    private final transient String region;
    private final AWSCognitoIdentityProvider cognitoIdentityProvider;

    /**
     * Create a CognitoHelper to help create an id_token to access services in API-gateway.
     *
     * @param userPoolId  id of the userpool
     * @param clientAppId clientapp secret from cognito
     * @param region      AWS region userpool is created in
     */
    public CognitoHelper(String userPoolId, String clientAppId, String region) {
        this.userPoolId = userPoolId;
        this.clientAppId = clientAppId;
        this.region = region;
        cognitoIdentityProvider = getCognitoIdentityProvider();
    }

    private String getPoolId() {
        return userPoolId;
    }

    private String getClientId() {
        return clientAppId;
    }

    /**
     * Create user in the user to the user pool.
     *
     * @param feideId     User name for the sign up
     * @param password    Password for the sign up
     * @param accessRight String containing wanted accessRights in token
     * @return username of user created from parameters.
     */
    public String createUser(String feideId, String password, String accessRight) {

        deleteUserIfExists(feideId);

        AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest();
        createUserRequest.setUserPoolId(getPoolId());
        createUserRequest.setUsername(feideId);
        createUserRequest.withTemporaryPassword(password);
        List<AttributeType> list = new ArrayList<>();

        list.add(new AttributeType().withName(CUSTOM_FEIDE_ID_ATTRIBUTE).withValue(feideId));
        list.add(new AttributeType().withName(CUSTOM_AFFILIATION_ATTRIBUTE).withValue(FEIDE_AFFILIATION));
        list.add(new AttributeType().withName(CUSTOM_APPLICATION_ATTRIBUTE).withValue(NVA_APPLICATION));
        list.add(new AttributeType().withName(CUSTOM_APPLICATION_ROLES_ATTRIBUTE).withValue(INSTITUTION_ADMIN_ROLE));
        list.add(new AttributeType().withName(CUSTOM_ACCESS_RIGHTS_ATTRIBUTE).withValue(accessRight));

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
            System.out.println("user created");
            return result.getUser().getUsername();
        } catch (Exception e) {
            logger.warn(PROBLEM_CREATING_USER_MESSAGE, feideId, e.getMessage());
            return null;
        }
    }

    private void deleteUserIfExists(String feideId) {
        try {
            getUsername(feideId).ifPresent(this::deleteUser);
        } catch (Exception e) {
            logger.warn("Tried to delete user:{}, {}", feideId,e.getMessage());
        }
    }

    /**
     * Update user accessRights attributes in AWS Cognito userpool.
     *
     * @param feideId      user identifier
     * @param accessRights String literal containing accessRights as wanted in id_token generated from AWS Cognito
     * @return If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body.
     *         If not successful a runtime exception is thrown
     */
    public AdminUpdateUserAttributesResult updateUserAttributes(String feideId, String accessRights) {
        List<AttributeType> list = List.of(
                new AttributeType()
                        .withName(CUSTOM_ACCESS_RIGHTS_ATTRIBUTE)
                        .withValue(accessRights));

        AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
                .withUserPoolId(getPoolId())
                .withUsername(feideId)
                .withUserAttributes(list);
        return cognitoIdentityProvider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
    }

    /**
     * Delete a user in Cognito userpool.
     *
     * @param feideId string identifying user in userpool
     * @return If the action is successful, the service sends back an HTTP 200 response with an empty HTTP body
     *         If not successful a runtime exception is thrown
     */
    public AdminDeleteUserResult deleteUser(String feideId) {
        AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                .withUserPoolId(getPoolId())
                .withUsername(feideId);
        return getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
    }

    public Optional<String> getUsername(String feideId) {
        ListUsersRequest listUsersRequest = new ListUsersRequest()
                .withUserPoolId(getPoolId())
                .withFilter("email="+feideId);
        var listUsersResult = getCognitoIdentityProvider().listUsers(listUsersRequest);
        return listUsersResult.getUsers().stream().findFirst().map(UserType::getUsername);
    }

    /**
     * Sign the user in (login) to userpool.
     *
     * @param username username
     * @param password password
     * @return result of operation containing credentials and tokens
     */
    public AdminInitiateAuthResult loginUser(String username, String password) {

        final Map<String, String> authParams = Map.of("USERNAME", username, "PASSWORD", password);
        final AdminInitiateAuthRequest initiateAuthRequest = new AdminInitiateAuthRequest()
                .withUserPoolId(getPoolId())
                .withClientId(getClientId())
                .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                .withAuthParameters(authParams);
        return cognitoIdentityProvider.adminInitiateAuth(initiateAuthRequest);
    }

    private AWSCognitoIdentityProvider getCognitoIdentityProvider() {
        AWSCredentials awsCredentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        return AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.fromName(region))
                .build();
    }

}
