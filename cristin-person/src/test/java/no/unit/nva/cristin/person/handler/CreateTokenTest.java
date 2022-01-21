package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import no.unit.nva.cognito.CognitoHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateTokenTest {

    public static final String REGION = "eu-west-1";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";
    private static final String CLIENT_APP_ID = "4qfhv3kl9qcr2knsfb8lhu1u40";

    @Test
    void shouldCreateAndDeleteUserShowToken() {
        CognitoHelper cognitoHelper = new CognitoHelper(USER_POOL_ID, CLIENT_APP_ID, REGION);

        final String feideId = "karate-user-administrator@sikt.no";
        final String password = "p@ssW0rd";

        String cognitoUserId = cognitoHelper.createUser(feideId, password, "EDIT_OWN_INSTITUTION_USERS,READ_DOI_REQUEST");
        assertNotNull(cognitoUserId);

        cognitoHelper.updateUserAttributes(feideId);

        AdminInitiateAuthResult loginResult = cognitoHelper.signInUserToAWSCognitoPool(feideId, password);
        assertNotNull(loginResult);

        String token = loginResult.getAuthenticationResult().getIdToken();
        assertNotNull(token);

        boolean deleted = cognitoHelper.deleteUser(feideId);
        assertTrue(deleted);

        AdminInitiateAuthResult deletedUserLoginResult = cognitoHelper.signInUserToAWSCognitoPool(feideId, password);
        assertNull(deletedUserLoginResult);

    }


}
