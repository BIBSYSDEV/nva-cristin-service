package no.unit.nva.utils;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class AwsCognitoRSAKeyProvider implements RSAKeyProvider {

    private final transient URL awsKidStoreUrl;
    private final transient JwkProvider provider;

    public AwsCognitoRSAKeyProvider(String awsCognitoRegion, String awsUserPoolId) {
        String url = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
                awsCognitoRegion,
                awsUserPoolId);
        try {
            awsKidStoreUrl = new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Invalid URL provided, URL=%s", url));
        }
        provider = new JwkProviderBuilder(awsKidStoreUrl).build();
    }

    @Override
    public RSAPublicKey getPublicKeyById(String kid) {
        try {
            return (RSAPublicKey) provider.get(kid).getPublicKey();
        } catch (JwkException e) {
            throw new RuntimeException(String.format("Failed to get JWT kid=%s from awsKidStoreUrl=%s",
                    kid,
                    awsKidStoreUrl));
        }
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return null;
    }

    @Override
    public String getPrivateKeyId() {
        return null;
    }
}
