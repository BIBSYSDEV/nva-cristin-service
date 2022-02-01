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
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.MessageActionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The CognitoHelper class abstracts the functionality of connecting to the Cognito user pool and Federated Identities.
 */
public class CognitoHelper {


    private final transient String userPoolId;
    private final transient String clientAppId;
    private final transient String region;
    private final AWSCognitoIdentityProvider cognitoIdentityProvider;

    /**
     * Create a CognitoHelper to help create a id_token to access services in API-gateway.
     * @param userPoolId id of the userpool
     * @param clientAppId clientapp secret from cognito
     * @param region AWS region userpool is created in
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
     * @return token to access API-gateway.
     */
    public String createUser(String feideId, String password, String accessRight) {

        AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest();
        createUserRequest.setUserPoolId(getPoolId());
        createUserRequest.setUsername(feideId);
        createUserRequest.withTemporaryPassword(password);
        List<AttributeType> list = new ArrayList<>();

        list.add(new AttributeType().withName("custom:feideId").withValue(feideId));
        list.add(new AttributeType().withName("custom:affiliation").withValue("feide:[member, employee, staff]"));
        list.add(new AttributeType().withName("custom:application").withValue("NVA"));
        list.add(new AttributeType().withName("custom:applicationRoles").withValue("Institution-admin"));
        list.add(new AttributeType().withName("custom:accessRights").withValue(accessRight));

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
            return result.getUser().getUsername();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Update user accessRights attributes.
     *
     * @param feideId user identifier
     * @param accessRights
     * @return result of operation
     */
    public AdminUpdateUserAttributesResult updateUserAttributes(String feideId, String accessRights) {
        List<AttributeType> list = List.of(
                new AttributeType()
                        .withName("custom:accessRights")
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
     * @return result of operation
     */
    public AdminDeleteUserResult deleteUser(String feideId) {
        AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                .withUserPoolId(getPoolId())
                .withUsername(feideId);
        return getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
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
        AWSCredentials awsCreds = new DefaultAWSCredentialsProviderChain().getCredentials();
        return AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(region))
                .build();
    }

}
