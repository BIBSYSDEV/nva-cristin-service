package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CognitoUtilCreateUsers {


    public static final String REGION = "eu-west-1";
    public static final String FEIDE_ID = "karate-user-administrator@sikt.no";
    public static final String PASSWORD = "p@ssW0rd";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";
    private static final String CLIENT_APP_ID = "4qfhv3kl9qcr2knsfb8lhu1u40";

    private static final Logger logger = LoggerFactory.getLogger(CognitoUtilCreateUsers.class);

    private static final CognitoUtil cognitoUtil = new CognitoUtil(USER_POOL_ID, CLIENT_APP_ID, REGION);

    @Tag("integrationTest")
    @Tag("createTestUsers")
    @Test
    void createTestUser() {
        if (isNull(cognitoUtil.loginUser(FEIDE_ID, PASSWORD))) {
            logger.info("createTestUser");
            cognitoUtil.deleteUser(FEIDE_ID);
            cognitoUtil.createUser(FEIDE_ID, PASSWORD);
            assertNotNull(cognitoUtil.loginUser(FEIDE_ID, PASSWORD));
        } else {
            logger.info("testuser not changed, exists and can login");
        }
    }

}
