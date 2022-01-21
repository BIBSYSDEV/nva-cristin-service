package no.unit.nva.cognito;

/*
// Copyright 2013-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
 */

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


    private final String userPoolId;
    private final String clientAppId;
    private final String region;
    private final AWSCognitoIdentityProvider cognitoIdentityProvider;


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
     * Sign up the user to the user pool
     *
     * @param feideId     User name for the sign up
     * @param password    Password for the sign up
     * @param accessRight
     * @return whether the call was successful or not.
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

    public void updateUserAttributes(String feideId) {
        List<AttributeType> list = List.of(
                new AttributeType()
                        .withName("custom:accessRights")
                        .withValue("EDIT_OWN_INSTITUTION_USERS,READ_DOI_REQUEST"));

        AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
                .withUserPoolId(getPoolId())
                .withUsername(feideId)
                .withUserAttributes(list);
        cognitoIdentityProvider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
    }


    public boolean deleteUser(String feideId) {
        AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                .withUserPoolId(getPoolId())
                .withUsername(feideId);

        AdminDeleteUserResult adminDeleteUserResult = getCognitoIdentityProvider().adminDeleteUser(deleteUserRequest);
        return adminDeleteUserResult != null;
    }


    public AdminInitiateAuthResult signInUserToAWSCognitoPool(String username, String password) {
        try {

            final Map<String, String> authParams = Map.of("USERNAME", username, "PASSWORD", password);
            final AdminInitiateAuthRequest initiateAuthRequest = new AdminInitiateAuthRequest()
                    .withUserPoolId(getPoolId())
                    .withClientId(getClientId())
                    .withAuthFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .withAuthParameters(authParams);

            return cognitoIdentityProvider.adminInitiateAuth(initiateAuthRequest);
        } catch (Exception e) {
            System.out.println("Exception occured during sign up user : " + e);
            return null;
        }
    }
    private AWSCognitoIdentityProvider getCognitoIdentityProvider() {
        AWSCredentials awsCreds = new DefaultAWSCredentialsProviderChain().getCredentials();
        AWSCognitoIdentityProvider cognitoIdentityProvider = AWSCognitoIdentityProviderClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(Regions.fromName(region))
                .build();
        return cognitoIdentityProvider;
    }

}
