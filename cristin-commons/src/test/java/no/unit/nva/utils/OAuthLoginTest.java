package no.unit.nva.utils;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import java.net.URI;
import org.junit.jupiter.api.Test;

class OAuthLoginTest {

    @Test
    void testLogin() {
        var redirectUri = URI.create("http://localhost:3000");
        var cognitoUrl = randomUri();
        var clientId = randomString();
        var username = randomString();
        var password = randomString();

        var httpClient = FakeCognitoHttpClient.builder()
                             .withAuthorizationCodeEndpoint()
                             .withTokenEndpoint()
                             .build();

        var oauth = new OAuthLogin(httpClient, clientId, redirectUri, cognitoUrl);
        var authorizationCode = oauth.getCode(username, password);

        assertThat(oauth.getAccessToken(authorizationCode), is(notNullValue()));
    }
}
