package no.unit.nva.utils;

import nva.commons.secrets.SecretsReader;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import static nva.commons.core.attempt.Try.attempt;

public class HttpUtils {


    public static final String SECRET_NAME = "cristinBasicAuth";
    private static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    private transient final String userName;
    private transient final String passWord;
    private final SecretsReader secretsReader;

    public HttpUtils()  {
        this(new SecretsReader());
    }

    public HttpUtils(SecretsReader secretsReader)  {
        userName = attempt(() -> secretsReader.fetchSecret(SECRET_NAME, USERNAME_KEY)).get();
        passWord = attempt(() -> secretsReader.fetchSecret(SECRET_NAME, PASSWORD_KEY)).get();
        this.secretsReader = secretsReader;
    }

    public Authenticator getBasicAuthenticator() {

        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, passWord.toCharArray());
            }
        };
    }

}
