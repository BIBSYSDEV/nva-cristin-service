package no.unit.nva.utils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;

@Tag("integrationTest")
class UserUtilsTest {

    @Test
    void createUserWithRoles() throws IOException, InterruptedException {

        String username = "ninja@test";
        String password = randomString();
        String nin = "27034425812";
        URI customerId = URI.create("https://api.dev.nva.aws.unit.no/customer/8e74a753-323d-4979-a3b0-19e35fa27fe5");
        Set<String> roles = Set.of("Creator");
        UserUtils.createUserWithRoles(username, password, nin, customerId, roles);

    }
}
