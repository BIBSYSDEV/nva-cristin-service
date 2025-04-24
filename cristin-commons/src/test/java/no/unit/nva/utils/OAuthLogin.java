package no.unit.nva.utils;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import no.unit.nva.commons.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuthLogin {

    private final HttpClient httpClient;
    private final URI cognitoUrl;
    private final String clientId;
    private final URI redirectUri;
    private static final Logger logger = LoggerFactory.getLogger(OAuthLogin.class);

    public OAuthLogin(HttpClient httpClient, String clientId, URI redirectUri, URI cognitoUrl) {
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.cognitoUrl = cognitoUrl;
    }

    public String getCode(String username, String password) {
        var loginUri = generateLoginUri(clientId, redirectUri, cognitoUrl);
        logger.info("URL: {}", loginUri);

        var encodedData = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                          "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
                          "&_csrf=cd1e2cfa-1b42-473d-862d-01a4e09e7b44";

        var request = HttpRequest.newBuilder()
                          .uri(loginUri)
                          .header("Content-Type", "application/x-www-form-urlencoded")
                          .header("Cookie", "XSRF-TOKEN=cd1e2cfa-1b42-473d-862d-01a4e09e7b44")
                          .header("Origin", cognitoUrl.toString())
                          .header("Referer", loginUri.toString())
                          .POST(BodyPublishers.ofString(encodedData))
                          .build();

        var response = attempt(() -> httpClient.send(request, HttpResponse.BodyHandlers.ofString())).orElseThrow();

        if (response.statusCode() == 302) {
            var location = response.headers().firstValue("Location").orElse(null);
            if (location != null) {
                logger.info("Redirect location: {}", location);
                return extractCodeFromLocation(location);
            } else {
                logger.warn("No Location header found in redirect.");
            }
        } else {
            logger.warn("Unexpected status code: {}", response.statusCode());
        }
        return null;
    }

    private static URI generateLoginUri(String clientId, URI redirectUri, URI cognitoUrl) {
        var baseUrl = cognitoUrl + "/login";
        var params = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                     "&response_type=code" +
                     "&scope=" + URLEncoder.encode(
            "aws.cognito.signin.user.admin email https://api.nva.unit.no/scopes/frontend openid phone profile",
            StandardCharsets.UTF_8) +
                     "&redirect_uri=" + URLEncoder.encode(redirectUri.toString(), StandardCharsets.UTF_8);
        return URI.create(baseUrl + "?" + params);
    }

    private static String extractCodeFromLocation(String location) {
        try {
            var uri = URI.create(location);
            var query = uri.getQuery();
            var queryParams = parseQueryParams(query);
            return queryParams.get("code");
        } catch (Exception e) {
            logger.error("Error parsing redirected URL: {}", e.getMessage());
            return null;
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        var queryParams = new HashMap<String, String>();
        for (var param : query.split("&")) {
            var pair = param.split("=");
            if (pair.length > 1) {
                queryParams.put(pair[0], pair[1]);
            }
        }
        return queryParams;
    }

    public String getAccessToken(String authorizationCode) {
        var postData = "grant_type=authorization_code" +
                       "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                       "&redirect_uri=" + URLEncoder.encode(redirectUri.toString(), StandardCharsets.UTF_8) +
                       "&code=" + URLEncoder.encode(authorizationCode, StandardCharsets.UTF_8);

        var request = HttpRequest.newBuilder()
                          .uri(URI.create(cognitoUrl + "/oauth2/token"))
                          .header("Content-Type", "application/x-www-form-urlencoded")
                          .POST(BodyPublishers.ofString(postData))
                          .build();

        var response = attempt(() -> httpClient.send(request, HttpResponse.BodyHandlers.ofString())).orElseThrow();

        if (response.statusCode() == 200) {
            logger.info("Successfully exchanged authorization code for access token.");
            var responseBody = response.body();

            return attempt(() -> JsonUtils.dtoObjectMapper.readValue(responseBody, Map.class)
                                     .get("access_token")
                                     .toString()).orElseThrow();
        } else {
            logger.error("Error exchanging authorization code for access token: {}", response.statusCode());
            return null;
        }
    }
}