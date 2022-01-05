package no.unit.nva.utils;

import nva.commons.secrets.SecretsReader;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class HttpUtils {


    public static final String SECRET_NAME = "cristinBasicAuth";
    private static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    private transient final String userName;
    private transient final String passWord;
//    private final SecretsReader secretsReader;

    public HttpUtils()  {
        userName = "nvatest";
        passWord = "zl5yxb-cke502-164uqh-84n023m";
//        this(new SecretsReader());
    }

//    public HttpUtils(SecretsReader secretsReader)  {
////        userName = secretsReader.fetchSecret(SECRET_NAME, USERNAME_KEY);
////        passWord = secretsReader.fetchSecret(SECRET_NAME, PASSWORD_KEY);
//        this.secretsReader = secretsReader;
//        userName = "nvatest";
//        passWord = "zl5yxb-cke502-164uqh-84n023m";
//
//    }


    public Authenticator getBasicAuthenticator() {

        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, passWord.toCharArray());
            }
        };
    }


//    private String getCristinBasicAuth() {
//        try {
//            SecretsReader secretsReader = new SecretsReader();
//            return secretsReader.fetchSecret(SECRET_NAME, secretKey);
//        } catch (ErrorReadingSecretException e) {
//            logger.error(CROSSREF_API_KEY_SECRET_NOT_FOUND_TEMPLATE, secretName, secretKey);
//            throw new RuntimeException(e);
//        }
//    }
}
