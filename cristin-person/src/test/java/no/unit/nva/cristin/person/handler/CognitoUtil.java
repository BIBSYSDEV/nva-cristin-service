package no.unit.nva.cristin.person.handler;

import no.unit.nva.cognito.CognitoHelper;

public class CognitoUtil {

    public static final String REGION = "eu-west-1";
    public static final String FEIDE_ID = "karate-user-administrator@sikt.no";
    public static final String PASSWORD = "p@ssW0rd";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";
    private static final String CLIENT_APP_ID = "4qfhv3kl9qcr2knsfb8lhu1u40";
    public static final String ACCESS_RIGHT = "EDIT_OWN_INSTITUTION_USERS,READ_DOI_REQUEST";

    /**
     * Create a user using default values.
     * @return id_token from loginUser()
     */
    public String createIdToken() {

        CognitoHelper cognitoHelper = new CognitoHelper(USER_POOL_ID, CLIENT_APP_ID, REGION);
        cognitoHelper.createUser(FEIDE_ID, PASSWORD, ACCESS_RIGHT);
        cognitoHelper.updateUserAttributes(FEIDE_ID);
        return cognitoHelper.loginUser(FEIDE_ID, PASSWORD).getAuthenticationResult().getIdToken();
    }

    /**
     * Delete the default user from Cognito Userpool.
     * @return true if operation is a success
     */
    public boolean deleteUser() {
        return new CognitoHelper(USER_POOL_ID, CLIENT_APP_ID, REGION).deleteUser(FEIDE_ID) != null;
    }

}
