package no.unit.nva.cristin.common.client;

import java.net.http.HttpClient.Version;
import nva.commons.secrets.SecretsReader;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;

import static nva.commons.core.attempt.Try.attempt;

/**
 * Creates an Authenticator to create a HttpClient with authentication for usage in ApiClient requests to Cristin API.
 */
public class CristinAuthenticator {

    public static final String SECRET_NAME = "CristinClientBasicAuth";
    public static final String PASSWORD_KEY = "password";
    public static final String USERNAME_KEY = "username";
    public static final SecretsReader SECRETS_READER = new SecretsReader();

    /**
     * Creates an Authenticator from credentials stored in AWS SecretsManager.
     *
     * @return Authenticator from resolved credentials
     */
    public static Authenticator getBasicAuthenticator() {

        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUserName(), getPassWord().toCharArray());
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
                   .version(Version.HTTP_1_1)
                   .authenticator(getBasicAuthenticator())
                   .connectTimeout(Duration.ofSeconds(15))
                   .build();
    }

    /**
     * Create value part of a basic authHeader.
     *
     * @return String containing encoded header
     */
    public static String basicAuthHeader() {
        return "Basic " + Base64.getEncoder().encodeToString((getUserName() + ":" + getPassWord()).getBytes());
    }

    private static String getPassWord() {
        return attempt(() -> SECRETS_READER.fetchSecret(SECRET_NAME, PASSWORD_KEY)).orElseThrow();
    }

    private static String getUserName() {
        return attempt(() -> SECRETS_READER.fetchSecret(SECRET_NAME, USERNAME_KEY)).orElseThrow();
    }

}
