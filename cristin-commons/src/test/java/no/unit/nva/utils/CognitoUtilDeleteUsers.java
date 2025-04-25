package no.unit.nva.utils;

import nva.commons.core.Environment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.utils.CognitoUtil.ADMIN_TESTUSER_ID_KEY;
import static no.unit.nva.utils.CognitoUtil.SIMPLE_TESTUSER_ID_KEY;

@Tag("integrationTest")
@Tag("deleteTestUsers")
public class CognitoUtilDeleteUsers {

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtilDeleteUsers.class);
    public static final String USER_DELETED_MSG = "User {} deleted!";

    @Test
    void deleteAdminUser() {
        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(ADMIN_TESTUSER_ID_KEY);
        CognitoUtil.deleteUser(feideUserName, AccessUtils.getUserPoolId());
        logger.info(USER_DELETED_MSG, feideUserName);
    }

    @Test
    void deleteSimpleTestUser() {
        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(SIMPLE_TESTUSER_ID_KEY);
        CognitoUtil.deleteUser(feideUserName, AccessUtils.getUserPoolId());
        logger.info(USER_DELETED_MSG, feideUserName);
    }


}
