package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.util.Optional;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.exception.GatewayTimeoutException.ERROR_MESSAGE_GATEWAY_TIMEOUT;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
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

public class FetchCristinPersonHandlerTest {

    private static final String NVA_API_GET_PERSON_RESPONSE_JSON =
        "nvaApiGetPersonResponse.json";
    private static final Map<String, String> ILLEGAL_PATH_PARAM = Map.of(ID, "string");
    private static final Map<String, String> ILLEGAL_QUERY_PARAMS = Map.of("somekey", "somevalue");
    private static final Map<String, String> VALID_PATH_PARAM = Map.of(ID, "12345");
    private static final Map<String, String> ZERO_QUERY_PARAMS = Collections.emptyMap();
    private static final String EMPTY_STRING = "";
    private static final String EXPECTED_CRISTIN_URI_WITH_IDENTIFIER =
        "https://api.cristin-test.uio.no/v2/persons/12345?lang=en,nb,nn";
    private static final Map<String, String> VALID_ORCID_PATH_PARAM = Map.of(ID, "1234-1234-1234-1234");
    private static final String EXPECTED_CRISTIN_URI_WITH_ORCID_IDENTIFIER =
        String.format("%s/persons/ORCID:1234-1234-1234-1234?lang=en,nb,nn", CRISTIN_API_URL);
    private static final int EXPECTED_HITS_SIZE_FOR_EMPLOYMENTS = 2;
    private static final String CRISTIN_GET_PERSON_RESPONSE_JSON = "cristinGetPersonResponse.json";
    private static final String CRISTIN_QUERY_EMPLOYMENT_RESPONSE_JSON = "cristinQueryEmploymentResponse.json";
    public static final String NORWEGIAN_NATIONAL_ID = "12345612345";

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
    void shouldReturnResponseWhenCallingEndpointWithValidIdentifier() throws IOException {
        var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);
        var expectedString = stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));
        var expected = OBJECT_MAPPER.readValue(expectedString, Person.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldThrowBadRequestWhenCallingEndpointWithAnyQueryParameters() throws IOException {
        var gatewayResponse = sendQuery(ILLEGAL_QUERY_PARAMS, VALID_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
            containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP));
    }

    @Test
    void shouldThrowBadRequestWhenPathParamIsNotANumberOrOrcid() throws IOException {
        var gatewayResponse = sendQuery(ZERO_QUERY_PARAMS, ILLEGAL_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
            containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID));
    }

    @Test
    void shouldReturnNotFoundToClientWhenCristinFetchReturnsNotFound() throws Exception {
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(EMPTY_STRING, 404))
            .when(apiClient).fetchGetResult(any(URI.class));

        handler = new FetchCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadGatewayWhenStatusCodeFromBackendSignalsError() throws Exception {
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(EMPTY_STRING, 500))
            .when(apiClient).fetchGetResult(any(URI.class));

        handler = new FetchCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
            containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void shouldHideInternalExceptionFromClientWhenBackendFail() throws Exception {
        apiClient = spy(apiClient);

        doThrow(RuntimeException.class).when(apiClient).generateGetResponse(any());
        handler = new FetchCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void shouldProduceCorrectCristinUriFromIdentifier() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        handler = new FetchCristinPersonHandler(apiClient, environment);
        sendQuery(null, VALID_PATH_PARAM);
        verify(apiClient).fetchGetResult(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_IDENTIFIER).getUri());
    }

    @Test
    void shouldReturnResponseWhenCallingEndpointWithValidOrcidIdentifier() throws IOException {
        var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_ORCID_PATH_PARAM).getBodyObject(Person.class);
        var expectedString = stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));
        var expected = OBJECT_MAPPER.readValue(expectedString, Person.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldProduceCorrectCristinUriFromOrcidIdentifier() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        handler = new FetchCristinPersonHandler(apiClient, environment);
        sendQuery(null, VALID_ORCID_PATH_PARAM);
        verify(apiClient).fetchGetResult(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_ORCID_IDENTIFIER).getUri());
    }

    @Test
    void shouldReturnGatewayTimeoutWhenFetchFromUpstreamServerTimesOut() throws Exception {
        var clientMock = mock(HttpClient.class);
        when(clientMock.<String>send(any(), any())).thenThrow(new HttpConnectTimeoutException(EMPTY_STRING));
        var apiClient = new CristinPersonApiClient(clientMock);
        handler = new FetchCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_GATEWAY_TIMEOUT));
    }

    @Test
    void shouldReturnEmploymentDataInResponseToClientWhenUpstreamHasEmploymentDataInPayload()
        throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(cristinPersonWithEmploymentsAsJson()))
            .when(apiClient).fetchGetResult(any(URI.class));
        handler = new FetchCristinPersonHandler(apiClient, environment);
        var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);

        assertThat(actual.getEmployments().size(), equalTo(EXPECTED_HITS_SIZE_FOR_EMPLOYMENTS));
    }

    @Test
    void shouldHaveNinInResponseWhenNinIsPresentInUpstream() throws Exception {
        var cristinPerson = randomCristinPerson();
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(cristinPerson.toString(), 200))
            .when(apiClient).fetchGetResult(any(URI.class));
        handler = new FetchCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(null, VALID_PATH_PARAM);
        var responseBody = gatewayResponse.getBodyObject(Person.class);
        var ninObject = extractNinObjectFromIdentifiers(responseBody).orElseThrow();

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(ninObject.getValue(), equalTo(NORWEGIAN_NATIONAL_ID));
    }

    private Optional<TypedValue> extractNinObjectFromIdentifiers(Person responseBody) {
        return responseBody.getIdentifiers().stream()
                   .filter(typedValue -> typedValue.getType().equals(NATIONAL_IDENTITY_NUMBER))
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
        var cristinPerson = OBJECT_MAPPER.readValue(
            stringFromResources(Path.of(CRISTIN_GET_PERSON_RESPONSE_JSON)), CristinPerson.class);
        var cristinPersonEmployments = asList(OBJECT_MAPPER.readValue(
            stringFromResources(Path.of(CRISTIN_QUERY_EMPLOYMENT_RESPONSE_JSON)), CristinPersonEmployment[].class));
        cristinPerson.setDetailedAffiliations(cristinPersonEmployments);
        return OBJECT_MAPPER.writeValueAsString(cristinPerson);
    }

    @Test
    void shouldNotHaveEmploymentFieldInResponseWhenNotInUpstreamPayload() throws IOException {
        var actual = sendQuery(ZERO_QUERY_PARAMS, VALID_PATH_PARAM).getBodyObject(Person.class);

        assertThat(actual.getEmployments(), equalTo(null));
    }

    private GatewayResponse<Person> sendQuery(Map<String, String> queryParams, Map<String, String> pathParam)
        throws IOException {

        var input = requestWithParams(queryParams, pathParam);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private InputStream requestWithParams(Map<String, String> queryParams, Map<String, String> pathParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(queryParams)
            .withPathParameters(pathParams)
            .build();
    }

}
