package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import nva.commons.core.Environment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static no.unit.nva.cognito.CognitoUtil.ADMIN_TESTUSER_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.ADMIN_TESTUSER_NIN_KEY;
import static no.unit.nva.cognito.CognitoUtil.ADMIN_TESTUSER_PASSWORD_KEY;
import static no.unit.nva.cognito.CognitoUtil.SIMPLE_TESTUSER_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.SIMPLE_TESTUSER_NIN_KEY;
import static no.unit.nva.cognito.CognitoUtil.SIMPLE_TESTUSER_PASSWORD_KEY;
import static no.unit.nva.utils.AccessUtils.getTestClientAppId;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integrationTest")
@Tag("createTestUsers")
public class UserUtilCreateUsers {

    private static final Logger logger = LoggerFactory.getLogger(UserUtilCreateUsers.class);
    public static final String CREATE_TEST_USER_WITH_USER_NAME = "createTestUser with UserName {}";
    public static final URI CUSTOMER =
        URI.create("https://api.dev.nva.aws.unit.no/customer/bb3d0c0c-5065-4623-9b98-5810983c2478");
    private Environment environment = new Environment();

    @Test
    void createAdminTestUser() throws IOException, InterruptedException {

        String userName = environment.readEnv(ADMIN_TESTUSER_ID_KEY);
        assertNotNull(userName);
        String password = environment.readEnv(ADMIN_TESTUSER_PASSWORD_KEY);
        assertNotNull(password);

        String nvaUserNin = environment.readEnv(ADMIN_TESTUSER_NIN_KEY);
        assertNotNull(password);

        createUser(userName, password, nvaUserNin, Set.of("App-Admin", "Institution-admin"), CUSTOMER);

    }

    @Test
    void createRegularTestUser() throws IOException, InterruptedException {

        final Environment environment = new Environment();
        String userName = environment.readEnv(SIMPLE_TESTUSER_ID_KEY);
        assertNotNull(userName);
        String password = environment.readEnv(SIMPLE_TESTUSER_PASSWORD_KEY);
        assertNotNull(password);
        String nvaUserNin = environment.readEnv(SIMPLE_TESTUSER_NIN_KEY);
        assertNotNull(nvaUserNin);

        createUser(userName, password, nvaUserNin, Set.of("Creator"), CUSTOMER);
    }


    private void createUser(String userName, String password, String nvaUserNin, Set<String> roles, URI customerId)
        throws IOException, InterruptedException {
        logger.info(CREATE_TEST_USER_WITH_USER_NAME, userName);
        UserUtils.createUserWithRoles(userName, password, nvaUserNin, customerId, roles);
        assertNotNull(userName);  // user is created
        assertNotNull(CognitoUtil.loginUser(userName, password, getTestClientAppId())); // verify login
    }
}
