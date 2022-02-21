package no.unit.nva.cristin.common.client;

import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import nva.commons.secrets.SecretsReader;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.time.Duration;

import static nva.commons.core.attempt.Try.attempt;

/**
 * Creates an Authenticator to create a HttpClient with authentication for usage in ApiClient requests to Cristin API.
 */
public class CristinAuthenticator {

    private static final String SECRET_NAME = "CristinClientBasicAuth";
    private static final String PASSWORD_KEY = "password";
    private static final String USERNAME_KEY = "username";

    /**
     * Creates an Authenticator from credentials stored in AWS SecretsManager.
     *
     * @return Authenticator from resolved credentials
     */
    public static Authenticator getBasicAuthenticator() {
        final SecretsReader secretsReader =
                new SecretsReader(AWSSecretsManagerClientBuilder.standard().withRegion("eu-west-1").build());

        String userName = attempt(() -> secretsReader.fetchSecret(SECRET_NAME, USERNAME_KEY)).orElseThrow();
        String passWord = attempt(() -> secretsReader.fetchSecret(SECRET_NAME, PASSWORD_KEY)).orElseThrow();
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, passWord.toCharArray());
            }
        };
    }

    /**
     * Create an HTTPClient with authentication.
     *
     * @return HTTPClient with authentication
     */
    public static HttpClient getHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .authenticator(getBasicAuthenticator())
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

}
