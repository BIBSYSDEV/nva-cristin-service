package no.unit.nva.cognito;

import java.util.Optional;

public class CognitoUtil {

    public static final String REGION = "eu-west-1";
    public static final String FEIDE_ID = "karate-user-administrator@sikt.no";
    public static final String PASSWORD = "p@ssW0rd";
    public static final String ACCESS_RIGHT = "EDIT_OWN_INSTITUTION_USERS,READ_DOI_REQUEST,EDIT_OWN_INSTITUTION_PROJECTS";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";
    private static final String CLIENT_APP_ID = "4qfhv3kl9qcr2knsfb8lhu1u40";

    /**
     * Create a user using default values.
     *
     * @return id_token from loginUser()
     */
    public String createIdToken() {

        CognitoHelper cognitoHelper = new CognitoHelper(USER_POOL_ID, CLIENT_APP_ID, REGION);

        Optional<String> token = cognitoHelper.loginUserAndReturnToken(FEIDE_ID, PASSWORD);
        if (!token.isPresent()) {
            cognitoHelper.createUser(FEIDE_ID, PASSWORD, ACCESS_RIGHT);
            token = cognitoHelper.loginUserAndReturnToken(FEIDE_ID, PASSWORD);
        }
        return token.get();
    }

    /**
     * Delete the default user from Cognito Userpool.
     *
     * @return true if operation is a success
     */
    public boolean deleteUser() {
        return new CognitoHelper(USER_POOL_ID, CLIENT_APP_ID, REGION).deleteUser(FEIDE_ID) != null;
    }

}
