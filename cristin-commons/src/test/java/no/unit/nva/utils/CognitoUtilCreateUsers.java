package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import nva.commons.core.Environment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static no.unit.nva.cognito.CognitoUtil.COGNITO_CLIENT_APP_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.COGNITO_USER_POOL_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.SIMPLE_TESTUSER_FEIDE_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.SIMPLE_TESTUSER_PASSWORD_KEY;
import static no.unit.nva.cognito.CognitoUtil.TESTUSER_FEIDE_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.TESTUSER_PASSWORD_KEY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integrationTest")
@Tag("createTestUsers")
public class CognitoUtilCreateUsers {

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtilCreateUsers.class);
    public static final String USER_UNCHANGED_AND_CAN_LOGIN_MSG = "testuser not changed, exists and can login";
    public static final String CREATE_TEST_USER_WITH_FEIDE_USER_NAME = "createTestUser with feideUserName {}";


    @Test
    void createTestUser() {

        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(TESTUSER_FEIDE_ID_KEY);
        assertNotNull(feideUserName);
        String password = environment.readEnv(TESTUSER_PASSWORD_KEY);
        assertNotNull(password);
        String clientAppId = environment.readEnv(COGNITO_CLIENT_APP_ID_KEY);
        assertNotNull(clientAppId);
        String userpoolId = environment.readEnv(COGNITO_USER_POOL_ID_KEY);
        assertNotNull(userpoolId);

        if (isNull(CognitoUtil.loginUser(feideUserName, password, userpoolId, clientAppId))) {
            logger.info(CREATE_TEST_USER_WITH_FEIDE_USER_NAME, feideUserName);
            CognitoUtil.deleteUser(feideUserName, userpoolId);
            String cognitoUserName = CognitoUtil.createUser(feideUserName, password, userpoolId, clientAppId);
            assertNotNull(cognitoUserName);  // user is created
            assertNotNull(CognitoUtil.loginUser(feideUserName, password, userpoolId, clientAppId)); // verify login
        } else {
            logger.info(USER_UNCHANGED_AND_CAN_LOGIN_MSG);
        }
    }

    @Test
    void createRegularTestUser() {

        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(SIMPLE_TESTUSER_FEIDE_ID_KEY);
        assertNotNull(feideUserName);
        String password = environment.readEnv(SIMPLE_TESTUSER_PASSWORD_KEY);
        assertNotNull(password);
        String clientAppId = environment.readEnv(COGNITO_CLIENT_APP_ID_KEY);
        assertNotNull(clientAppId);
        String userpoolId = environment.readEnv(COGNITO_USER_POOL_ID_KEY);
        assertNotNull(userpoolId);

        if (isNull(CognitoUtil.loginUser(feideUserName, password, userpoolId, clientAppId))) {
            logger.info(CREATE_TEST_USER_WITH_FEIDE_USER_NAME, feideUserName);
            CognitoUtil.deleteUser(feideUserName, userpoolId);
            String cognitoUserName = CognitoUtil.createUser(feideUserName, password, userpoolId, clientAppId);
            assertNotNull(cognitoUserName);  // user is created
            assertNotNull(CognitoUtil.loginUser(feideUserName, password, userpoolId, clientAppId)); // verify login
        } else {
            logger.info(USER_UNCHANGED_AND_CAN_LOGIN_MSG);
        }
    }



}
