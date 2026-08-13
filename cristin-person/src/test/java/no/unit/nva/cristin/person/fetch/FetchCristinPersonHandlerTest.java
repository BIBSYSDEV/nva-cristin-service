package no.unit.nva.cristin.person.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSONS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.exception.TemporaryRedirectException.TEMPORARY_REDIRECT;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static nva.commons.core.paths.UriWrapper.fromUri;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.TemporaryRedirectException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.logutils.LogRecorder;
import org.apache.hc.core5.http.HttpHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class FetchCristinPersonHandlerTest {

  private static final String NVA_API_GET_PERSON_RESPONSE_JSON = "nvaApiGetPersonResponse.json";
  private static final Map<String, String> ILLEGAL_PATH_PARAM = Map.of(ID, "string");
  private static final Map<String, String> ILLEGAL_QUERY_PARAMS = Map.of("somekey", "somevalue");
  private static final Map<String, String> VALID_PATH_PARAM = Map.of(ID, "12345");
  private static final Map<String, String> ZERO_QUERY_PARAMS = Collections.emptyMap();
  private static final String EMPTY_STRING = "";
  private static final String EXPECTED_CRISTIN_URI_WITH_IDENTIFIER =
      "https://api.cristin-test.uio.no/v2/persons/12345";
  private static final Map<String, String> VALID_ORCID_PATH_PARAM =
      Map.of(ID, "1234-1234-1234-1234");
  private static final String EXPECTED_CRISTIN_URI_WITH_ORCID_IDENTIFIER =
      String.format("%s/persons/ORCID:1234-1234-1234-1234", CRISTIN_API_URL);
  private static final int EXPECTED_HITS_SIZE_FOR_EMPLOYMENTS = 2;
  private static final String CRISTIN_GET_PERSON_RESPONSE_JSON = "cristinGetPersonResponse.json";
  private static final String CRISTIN_QUERY_EMPLOYMENT_RESPONSE_JSON =
      "cristinQueryEmploymentResponse.json";
  public static final String NORWEGIAN_NATIONAL_ID = "12345612345";
  public static final String CRISTIN_PERSON_NVI_VERIFIED_JSON = "cristinPersonNviVerified.json";
  public static final String NVA_API_GET_PERSON_NVI_VERIFIED_JSON =
      "nvaApiGetPersonNviVerified.json";
  private static final String MERGED_INTO_IDENTIFIER = "5647";
  private static final String EXPECTED_NVA_LOCATION_FOR_MERGED_PERSON =
      "https://%s/%s/person/%s".formatted(DOMAIN_NAME, BASE_PATH, MERGED_INTO_IDENTIFIER);
  private static final String CRISTIN_URI_WITHOUT_PATH = "https://www.cristin.no";
  private static final String INSTITUTIONS_PATH = "institutions";
  private static final String PERSONS_PATH_CAPITALIZED = "Persons";
  private static final String SLASH = "/";
  private static final Map<String, String> PATH_PARAM_WITH_LEADING_ZEROS = Map.of(ID, "0012345");

  private CristinPersonApiClient apiClient;
  private final Environment environment = new Environment();
  private Context context;
  private ByteArrayOutputStream output;
  private FetchCristinPersonHandler handler;

  @BeforeEach
  void setUp() {
    apiClient = new CristinPersonApiClientStub();
    context = mock(Context.class);
    output = new ByteArrayOutputStream();
    handler = new FetchCristinPersonHandler(apiClient, environment);
  }

  @Test
  void shouldReturnResponseWhenCallingEndpointWithValidIdentifier() throws Exception {
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBody();
    var expected = stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));

    JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  void shouldThrowBadRequestWhenCallingEndpointWithAnyQueryParameters() throws IOException {
    var gatewayResponse = sendQuery(ILLEGAL_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
    assertEquals(
        APPLICATION_PROBLEM_JSON.toString(),
        gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertThat(
        gatewayResponse.getBody(),
        containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP));
  }

  @Test
  void shouldThrowBadRequestWhenPathParamIsNotANumberOrOrcid() throws IOException {
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, ILLEGAL_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
    assertEquals(
        APPLICATION_PROBLEM_JSON.toString(),
        gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertThat(
        gatewayResponse.getBody(),
        containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID));
  }

  @Test
  void shouldReturnNotFoundToClientWhenCristinFetchReturnsNotFound() throws Exception {
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(EMPTY_STRING, 404))
        .when(apiClient)
        .fetchGetResult(any(URI.class));

    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
  }

  @Test
  void shouldReturnBadGatewayWhenStatusCodeFromBackendSignalsError() throws Exception {
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(EMPTY_STRING, 500))
        .when(apiClient)
        .fetchGetResult(any(URI.class));

    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
    assertEquals(
        APPLICATION_PROBLEM_JSON.toString(),
        gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
  }

  @Test
  void shouldHideInternalExceptionFromClientWhenBackendFail() throws Exception {
    apiClient = spy(apiClient);

    doThrow(RuntimeException.class).when(apiClient).generateGetResponse(any());
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
    assertEquals(
        APPLICATION_PROBLEM_JSON.toString(),
        gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
  }

  @Test
  void shouldProduceCorrectCristinUriFromIdentifier() throws IOException, ApiGatewayException {
    apiClient = spy(apiClient);
    handler = new FetchCristinPersonHandler(apiClient, environment);
    sendQuery(null, VALID_PATH_PARAM);
    verify(apiClient).fetchGetResult(fromUri(EXPECTED_CRISTIN_URI_WITH_IDENTIFIER).getUri());
  }

  @Test
  void shouldReturnResponseWhenCallingEndpointWithValidOrcidIdentifier() throws Exception {
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_ORCID_PATH_PARAM).getBody();
    var expected = stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));

    JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
  }

  @Test
  void shouldProduceCorrectCristinUriFromOrcidIdentifier() throws IOException, ApiGatewayException {
    apiClient = spy(apiClient);
    handler = new FetchCristinPersonHandler(apiClient, environment);
    sendQuery(null, VALID_ORCID_PATH_PARAM);
    verify(apiClient).fetchGetResult(fromUri(EXPECTED_CRISTIN_URI_WITH_ORCID_IDENTIFIER).getUri());
  }

  @Test
  void shouldReturnHttpRequestFailedExceptionWhenFetchFromUpstreamServerTimesOut()
      throws Exception {
    var clientMock = mock(HttpClient.class);
    when(clientMock.<String>send(any(), any()))
        .thenThrow(new HttpConnectTimeoutException(EMPTY_STRING));
    var apiClient = new CristinPersonApiClient(clientMock);
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
    assertEquals(
        APPLICATION_PROBLEM_JSON.toString(),
        gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
  }

  @Test
  void
      shouldReturnEmploymentDataInResponseToClientWhenUpstreamHasEmploymentDataInPayloadAndUserAuthorized()
          throws IOException, ApiGatewayException {
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(cristinPersonWithEmploymentsAsJson()))
        .when(apiClient)
        .fetchGetResultWithAuthentication(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var actual = sendAuthorizedQuery(MANAGE_OWN_AFFILIATION).getBodyObject(Person.class);

    assertThat(actual.employments().size(), equalTo(EXPECTED_HITS_SIZE_FOR_EMPLOYMENTS));
  }

  @Test
  void shouldNotReturnEmploymentFieldWhenUserNotAuthorizedToSeeItEvenIfInUpstreamPayload()
      throws IOException, ApiGatewayException {
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(cristinPersonWithEmploymentsAsJson()))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);

    assertThat(actual.employments(), equalTo(null));
  }

  @ParameterizedTest
  @MethodSource("accessRightProvider")
  void shouldHaveNinInResponseWhenNinIsPresentInUpstreamAndClientIsAuthenticated(
      AccessRight accessRight) throws Exception {

    var cristinPerson = randomCristinPerson();
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(cristinPerson.toString(), 200))
        .when(apiClient)
        .fetchGetResultWithAuthentication(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendAuthorizedQuery(accessRight);
    var responseBody = gatewayResponse.getBodyObject(Person.class);
    var ninObject = extractNinObjectFromIdentifiers(responseBody).orElseThrow();

    assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    assertThat(ninObject.value(), equalTo(NORWEGIAN_NATIONAL_ID));
  }

  @Test
  void shouldNotHaveNinInResponseWhenClientIsNotAuthenticatedButNinIsPresentInUpstream()
      throws Exception {
    var cristinPerson = randomCristinPerson();
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(cristinPerson.toString(), 200))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);
    var responseBody = gatewayResponse.getBodyObject(Person.class);
    var ninObject = extractNinObjectFromIdentifiers(responseBody).orElse(null);
    verify(apiClient).fetchGetResult(any());

    assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    assertThat(ninObject, equalTo(null));
  }

  @Test
  void shouldAddLangToHttpRequestBeforeSendingToCristin() throws Exception {
    var cristinPerson =
        OBJECT_MAPPER.readValue(
            stringFromResources(Path.of(CRISTIN_GET_PERSON_RESPONSE_JSON)), CristinPerson.class);
    var response = new HttpResponseFaker(cristinPerson.toString(), HTTP_OK);
    var mockHttpClient = mock(HttpClient.class);
    when(mockHttpClient.<String>send(any(), any())).thenReturn(response);
    apiClient = spy(new CristinPersonApiClient(mockHttpClient));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    sendQuery(null, VALID_PATH_PARAM);

    var captor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(mockHttpClient).send(captor.capture(), any());

    var expected = "https://api.cristin-test.uio.no/v2/persons/12345?lang=en%2Cnb%2Cnn";
    var actual = captor.getValue().uri().toString();

    assertThat(actual, equalTo(expected));
  }

  @Test
  void shouldNotHaveEmploymentFieldInResponseWhenNotInUpstreamPayload() throws IOException {
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);

    assertThat(actual.employments(), equalTo(null));
  }

  @Test
  void shouldReturnResponseContainingNviDataWhenPresentInUpstream() throws Exception {
    var json = readFromResources(CRISTIN_PERSON_NVI_VERIFIED_JSON);
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(json)).when(apiClient).fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBody();
    var expected = readFromResources(NVA_API_GET_PERSON_NVI_VERIFIED_JSON);

    JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
  }

  @Test
  void shouldReturnPersonAsVerifiedWhenThatPersonIsNotRegularVerifiedButNviVerified()
      throws Exception {
    var json = readFromResources(CRISTIN_PERSON_NVI_VERIFIED_JSON);
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(json)).when(apiClient).fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);

    assertThat(actual.verified(), equalTo(true));
  }

  @Test
  void shouldNotReturnPersonAsVerifiedWhenThatPersonIsNeitherRegularVerifiedOrNviVerified()
      throws Exception {
    var cristinPerson = randomCristinPerson();
    cristinPerson.setNorwegianNationalId(null);
    cristinPerson.setIdentifiedCristinPerson(false);
    var json = cristinPerson.toString();
    apiClient = spy(apiClient);
    doReturn(new HttpResponseFaker(json)).when(apiClient).fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);

    assertThat(actual.verified(), equalTo(false));
  }

  @Test
  void shouldReturnTemporaryRedirectToNewPersonWhenUpstreamRedirectsToPersonMergedInto()
      throws Exception {
    apiClient = spy(apiClient);
    doReturn(responseRedirectedToPerson(MERGED_INTO_IDENTIFIER))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(TEMPORARY_REDIRECT, gatewayResponse.getStatusCode());
    assertThat(
        gatewayResponse.getHeaders().get(HttpHeaders.LOCATION),
        equalTo(EXPECTED_NVA_LOCATION_FOR_MERGED_PERSON));
  }

  @Test
  void shouldReturnTemporaryRedirectToNewPersonWhenUpstreamRedirectsAndClientIsAuthorized()
      throws Exception {
    apiClient = spy(apiClient);
    doReturn(responseRedirectedToPerson(MERGED_INTO_IDENTIFIER))
        .when(apiClient)
        .fetchGetResultWithAuthentication(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendAuthorizedQuery(MANAGE_OWN_AFFILIATION);

    assertEquals(TEMPORARY_REDIRECT, gatewayResponse.getStatusCode());
    assertThat(
        gatewayResponse.getHeaders().get(HttpHeaders.LOCATION),
        equalTo(EXPECTED_NVA_LOCATION_FOR_MERGED_PERSON));
  }

  @Test
  void shouldReturnPersonDataWhenLookingUpByOrcidEvenThoughUpstreamRedirectsToCristinIdentifier()
      throws Exception {
    apiClient = spy(apiClient);
    doReturn(responseRedirectedToPerson(MERGED_INTO_IDENTIFIER))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_ORCID_PATH_PARAM);

    assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
  }

  @Test
  void shouldReturnNotFoundWhenUpstreamRedirectsToPersonThatDoesNotExist() throws Exception {
    apiClient = spy(apiClient);
    doReturn(
            HttpResponseFaker.respondedFromUri(
                EMPTY_STRING,
                HttpURLConnection.HTTP_NOT_FOUND,
                cristinUriForPerson(MERGED_INTO_IDENTIFIER)))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
  }

  @Test
  void shouldNotLogStackTraceWhenPersonIsMergedIntoAnother() throws Exception {
    final var logRecorder = LogRecorder.forRoot(FetchCristinPersonHandler.class);
    apiClient = spy(apiClient);
    doReturn(responseRedirectedToPerson(MERGED_INTO_IDENTIFIER))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(TEMPORARY_REDIRECT, gatewayResponse.getStatusCode());
    Assertions.assertThat(logRecorder.messages())
        .noneMatch(message -> message.contains(TemporaryRedirectException.class.getName()));
  }

  @Test
  void shouldNotRedirectWhenUpstreamIdentifierIsNumericallyEqualButWrittenDifferently()
      throws Exception {
    apiClient = spy(apiClient);
    doReturn(responseRedirectedToPerson(VALID_PATH_PARAM.get(ID)))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, PATH_PARAM_WITH_LEADING_ZEROS);

    assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
  }

  @Test
  void shouldRedirectWhenUpstreamPersonUriHasTrailingSlash() throws Exception {
    apiClient = spy(apiClient);
    doReturn(
            HttpResponseFaker.respondedFromUri(
                readFromResources(CRISTIN_GET_PERSON_RESPONSE_JSON),
                URI.create(cristinUriForPerson(MERGED_INTO_IDENTIFIER) + SLASH)))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(TEMPORARY_REDIRECT, gatewayResponse.getStatusCode());
    assertThat(
        gatewayResponse.getHeaders().get(HttpHeaders.LOCATION),
        equalTo(EXPECTED_NVA_LOCATION_FOR_MERGED_PERSON));
  }

  @Test
  void shouldRedirectWhenUpstreamPersonPathIsSpelledWithDifferentCasing() throws Exception {
    apiClient = spy(apiClient);
    doReturn(
            HttpResponseFaker.respondedFromUri(
                readFromResources(CRISTIN_GET_PERSON_RESPONSE_JSON),
                getCristinUri(MERGED_INTO_IDENTIFIER, PERSONS_PATH_CAPITALIZED)))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(TEMPORARY_REDIRECT, gatewayResponse.getStatusCode());
    assertThat(
        gatewayResponse.getHeaders().get(HttpHeaders.LOCATION),
        equalTo(EXPECTED_NVA_LOCATION_FOR_MERGED_PERSON));
  }

  @Test
  void shouldNotRedirectWhenUpstreamRespondsFromResourceThatIsNotAPerson() throws Exception {
    apiClient = spy(apiClient);
    doReturn(
            HttpResponseFaker.respondedFromUri(
                readFromResources(CRISTIN_GET_PERSON_RESPONSE_JSON),
                getCristinUri(MERGED_INTO_IDENTIFIER, INSTITUTIONS_PATH)))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
  }

  @Test
  void shouldReturnPersonWhenUpstreamRespondsFromUriWithoutAnyPath() throws Exception {
    apiClient = spy(apiClient);
    doReturn(
            HttpResponseFaker.respondedFromUri(
                readFromResources(CRISTIN_GET_PERSON_RESPONSE_JSON),
                URI.create(CRISTIN_URI_WITHOUT_PATH)))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
    handler = new FetchCristinPersonHandler(apiClient, environment);
    var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM);

    assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
  }

  private HttpResponseFaker responseRedirectedToPerson(String identifier) {
    return HttpResponseFaker.respondedFromUri(
        readFromResources(CRISTIN_GET_PERSON_RESPONSE_JSON), cristinUriForPerson(identifier));
  }

  private URI cristinUriForPerson(String identifier) {
    return getCristinUri(identifier, PERSONS_PATH);
  }

  private Optional<TypedValue> extractNinObjectFromIdentifiers(Person responseBody) {
    return responseBody.identifiers().stream()
        .filter(typedValue -> typedValue.type().equals(NATIONAL_IDENTITY_NUMBER))
        .findAny();
  }

  private CristinPerson randomCristinPerson() {
    var cristinPerson = new CristinPerson();
    cristinPerson.setCristinPersonId(VALID_PATH_PARAM.get(ID));
    cristinPerson.setFirstName(randomString());
    cristinPerson.setSurname(randomString());
    cristinPerson.setNorwegianNationalId(NORWEGIAN_NATIONAL_ID);
    return cristinPerson;
  }

  private String cristinPersonWithEmploymentsAsJson() throws JsonProcessingException {
    var cristinPerson =
        OBJECT_MAPPER.readValue(
            stringFromResources(Path.of(CRISTIN_GET_PERSON_RESPONSE_JSON)), CristinPerson.class);
    var cristinPersonEmployments =
        asList(
            OBJECT_MAPPER.readValue(
                stringFromResources(Path.of(CRISTIN_QUERY_EMPLOYMENT_RESPONSE_JSON)),
                CristinPersonEmployment[].class));
    cristinPerson.setDetailedAffiliations(cristinPersonEmployments);
    return OBJECT_MAPPER.writeValueAsString(cristinPerson);
  }

  private static String readFromResources(String json) {
    return IoUtils.stringFromResources(Path.of(json));
  }

  private GatewayResponse<Person> sendQuery(
      Map<String, String> queryParams, Map<String, String> pathParam) throws IOException {

    var input = requestWithParams(queryParams, pathParam);
    handler.handleRequest(input, output, context);
    return GatewayResponse.fromOutputStream(output, Person.class);
  }

  private InputStream requestWithParams(
      Map<String, String> queryParams, Map<String, String> pathParams)
      throws JsonProcessingException {

    return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
        .withBody(null)
        .withQueryParameters(queryParams)
        .withPathParameters(pathParams)
        .build();
  }

  private GatewayResponse<Person> sendAuthorizedQuery(AccessRight accessRight) throws IOException {
    var customerId = randomUri();
    var input =
        new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(VALID_PATH_PARAM)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, accessRight)
            .build();
    handler.handleRequest(input, output, context);
    return GatewayResponse.fromOutputStream(output, Person.class);
  }

  private static Stream<Arguments> accessRightProvider() {
    return Stream.of(Arguments.of(MANAGE_OWN_AFFILIATION), Arguments.of(MANAGE_CUSTOMERS));
  }
}
