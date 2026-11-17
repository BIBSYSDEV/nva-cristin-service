package no.unit.nva.utils;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserUtils {

  private static final Logger logger = LoggerFactory.getLogger(UserUtils.class);
  public static final String RESPONSE_FROM_USER_CREATE = "Response from user create={}";
  public static final String USER_CREATED_WITH_ROLES = "User {} created with role(s) '{}'";
  private static final String RESPONSE_FROM_PERSON_LOOKUP = "Response from person lookup={}";
  private static final String BEARER = "Bearer ";

  private UserUtils() {
    // NO-OP
  }

  /** Creates user in Cognito used for Karate tests. */
  public static void createUserWithRoles(
      String username,
      String password,
      String nationalIdentityNumber,
      URI customerId,
      Set<String> roles)
      throws IOException, InterruptedException {

    final String poolId = AccessUtils.getUserPoolId();
    final String appClientClientId = AccessUtils.getTestClientAppId();

    createNvaUserWithRoles(nationalIdentityNumber, customerId, roles);
    var personId = fetchPersonId(nationalIdentityNumber);

    CognitoUtil.deleteUser(username, poolId);
    CognitoUtil.adminCreateUser(
        username, password, nationalIdentityNumber, personId, poolId, appClientClientId);

    logger.info(USER_CREATED_WITH_ROLES, username, roles);
  }

  /** Looks up the person URI that identity service needs on a password user to let it log in. */
  private static URI fetchPersonId(String nationalIdentityNumber)
      throws IOException, InterruptedException {
    var lookupUri =
        new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild("cristin")
            .addChild("person")
            .addChild("identityNumber")
            .getUri();
    var lookupBody =
        JsonUtils.dtoObjectMapper.writeValueAsString(
            Map.of("type", "NationalIdentificationNumber", "value", nationalIdentityNumber));
    var lookupRequest =
        HttpRequest.newBuilder(lookupUri)
            .header(AUTHORIZATION, BEARER + AccessUtils.getBackendAccessToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(lookupBody))
            .build();
    try (var client = createHttpClient()) {
      var response = client.send(lookupRequest, ofString(StandardCharsets.UTF_8));
      logger.info(RESPONSE_FROM_PERSON_LOOKUP, response);
      return URI.create(JsonUtils.dtoObjectMapper.readTree(response.body()).get("id").asText());
    }
  }

  private static void createNvaUserWithRoles(
      String nationalIdentityNumber, URI customerId, Set<String> roles)
      throws IOException, InterruptedException {
    try (var client = createHttpClient()) {
      final var response =
          client.send(
              createHttpRequest(nationalIdentityNumber, customerId, roles),
              ofString(StandardCharsets.UTF_8));
      logger.info(RESPONSE_FROM_USER_CREATE, response);
    }
  }

  private static HttpClient createHttpClient() {
    return HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .connectTimeout(Duration.ofSeconds(30))
        .build();
  }

  private static HttpRequest createHttpRequest(
      String nationalIdentityNumber, URI customerId, Set<String> roles)
      throws IOException, InterruptedException {
    var roleDtos = roles.stream().map(RoleDto::fromRolename).collect(Collectors.toSet());
    final var userRoles = new UserRoles(nationalIdentityNumber, customerId, roleDtos);
    final var body = JsonUtils.dtoObjectMapper.writeValueAsString(userRoles);
    return HttpRequest.newBuilder(createUserServiceUri())
        .header(AUTHORIZATION, BEARER + AccessUtils.getBackendAccessToken())
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }

  private static URI createUserServiceUri() {
    return new UriWrapper(HTTPS, DOMAIN_NAME).addChild("users-roles").addChild("users").getUri();
  }

  private record UserRoles(
      @JsonProperty("nationalIdentityNumber") String nationalIdentityNumber,
      @JsonProperty("customerId") URI customerId,
      @JsonProperty("roles") Set<RoleDto> roles) {}

  private record RoleDto(
      @JsonProperty("type") String type, @JsonProperty("rolename") String rolename) {

    private static final String ROLE_TYPE = "Role";

    private static RoleDto fromRolename(String rolename) {
      return new RoleDto(ROLE_TYPE, rolename);
    }
  }
}
