package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import nva.commons.core.Environment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static no.unit.nva.cognito.CognitoUtil.COGNITO_USER_POOL_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.TESTUSER_FEIDE_ID_KEY;

@Tag("integrationTest")
public class CognitoUtilDeleteUsers {



    @Tag("deleteTestUsers")
    @Test
    void deleteTestUser() {
        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(TESTUSER_FEIDE_ID_KEY);
        String userpoolId = environment.readEnv(COGNITO_USER_POOL_ID_KEY);

        System.out.println("deleteTestUser");
        CognitoUtil.deleteUser(feideUserName, userpoolId);
    }

}
