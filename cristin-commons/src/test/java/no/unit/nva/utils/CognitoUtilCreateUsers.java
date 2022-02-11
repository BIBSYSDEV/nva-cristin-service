package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CognitoUtilCreateUsers {

    public static final String FEIDE_ID = "karate-user-administrator@sikt.no";
    public static final String PASSWORD = "p@ssW0rd";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";
    private static final String CLIENT_APP_ID = "4qfhv3kl9qcr2knsfb8lhu1u40";

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtilCreateUsers.class);


    @Tag("integrationTest")
    @Tag("createTestUsers")
    @Test
    void createTestUser() {
        if (isNull(CognitoUtil.loginUser(FEIDE_ID, PASSWORD, USER_POOL_ID, CLIENT_APP_ID))) {
            logger.info("createTestUser");
            CognitoUtil.deleteUser(FEIDE_ID, USER_POOL_ID);
            CognitoUtil.createUser(FEIDE_ID, PASSWORD, USER_POOL_ID);
            assertNotNull(CognitoUtil.loginUser(FEIDE_ID, PASSWORD, USER_POOL_ID, CLIENT_APP_ID));
        } else {
            logger.info("testuser not changed, exists and can login");
        }
    }

}
