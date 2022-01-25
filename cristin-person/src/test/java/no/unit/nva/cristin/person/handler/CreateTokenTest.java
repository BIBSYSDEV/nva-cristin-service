package no.unit.nva.cristin.person.handler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateTokenTest {

    @Tag("integrationTest")
    @Test
    void shouldCreateCognitoUser() {
        CognitoUtil cognitoUtil = new CognitoUtil();
        assertNotNull(cognitoUtil.createIdToken());
        assertTrue(cognitoUtil.deleteUser());
    }
}
