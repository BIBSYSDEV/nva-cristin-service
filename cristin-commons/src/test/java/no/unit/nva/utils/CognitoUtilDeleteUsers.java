package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integrationTest")
public class CognitoUtilDeleteUsers {

    public static final String FEIDE_ID = "karate-user-administrator@sikt.no";
    private static final String USER_POOL_ID = "eu-west-1_DNRmDPtxY";


    @Tag("deleteTestUsers")
    @Test
    void deleteTestUser() {
        System.out.println("deleteTestUser");
        CognitoUtil.deleteUser(FEIDE_ID, USER_POOL_ID);
    }

}
