package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integrationTest")
@Tag("createTestUsers")
public class CognitoUtilCreateUsers {


    public static final String REGION = "eu-west-1";
    public static final String FEIDE_ID = "karate-user-administrator@sikt.no";
    public static final String PASSWORD = "p@ssW0rd";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";
    private static final String CLIENT_APP_ID = "4qfhv3kl9qcr2knsfb8lhu1u40";

    private static final CognitoUtil cognitoUtil = new CognitoUtil(USER_POOL_ID, CLIENT_APP_ID, REGION);

    @Test

    void createTestUser() {
        System.out.println("createTestUser");
        cognitoUtil.deleteUser(FEIDE_ID);
        cognitoUtil.createUser(FEIDE_ID, PASSWORD);
    }

}
