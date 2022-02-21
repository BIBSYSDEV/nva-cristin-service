package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import nva.commons.core.Environment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cognito.CognitoUtil.COGNITO_USER_POOL_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.TESTUSER_FEIDE_ID_KEY;

@Tag("integrationTest")
@Tag("deleteTestUsers")
public class CognitoUtilDeleteUsers {

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtilDeleteUsers.class);
    public static final String USER_DELETED_MSG = "User {} deleted!";

    @Test
    void deleteTestUser() {
        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(TESTUSER_FEIDE_ID_KEY);
        String userpoolId = environment.readEnv(COGNITO_USER_POOL_ID_KEY);
        CognitoUtil.deleteUser(feideUserName, userpoolId);
        logger.info(USER_DELETED_MSG, feideUserName);
    }

}
