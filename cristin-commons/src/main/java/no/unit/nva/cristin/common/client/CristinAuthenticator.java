package no.unit.nva.cristin.common.client;

import java.net.http.HttpClient.Version;
import nva.commons.secrets.SecretsReader;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static nva.commons.core.attempt.Try.attempt;

/**
 * Creates an Authenticator to create a HttpClient with authentication for usage in ApiClient requests to Cristin API.
 */
public class CristinAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(CristinAuthenticator.class);

    public static final String SECRET_NAME = "CristinClientBasicAuth";
    public static final String PASSWORD_KEY = "password";
    public static final String USERNAME_KEY = "username";
    public static final SecretsReader SECRETS_READER = new SecretsReader();
    public static final String USING_AN_AUTHORIZED_HTTP_CLIENT = "Using an authorized Http Client";
    public static final String USING_A_BASIC_AUTH_HEADER = "Using basic auth header in upstream request";

    /**
     * Creates an Authenticator from credentials stored in AWS SecretsManager.
     *
     * @return Authenticator from resolved credentials
     */
    private static Authenticator getBasicAuthenticator() {

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
        logger.info(USING_AN_AUTHORIZED_HTTP_CLIENT);

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
        logger.info(USING_A_BASIC_AUTH_HEADER);

        return "Basic " + Base64.getEncoder().encodeToString((getUserName() + ":" + getPassWord()).getBytes());
    }

    private static String getPassWord() {
        return attempt(() -> SECRETS_READER.fetchSecret(SECRET_NAME, PASSWORD_KEY)).orElseThrow();
    }

    private static String getUserName() {
        return attempt(() -> SECRETS_READER.fetchSecret(SECRET_NAME, USERNAME_KEY)).orElseThrow();
    }

}
