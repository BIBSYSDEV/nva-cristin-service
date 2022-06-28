package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import java.util.ArrayList;
import java.util.stream.IntStream;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static no.unit.nva.exception.GatewayTimeoutException.ERROR_MESSAGE_GATEWAY_TIMEOUT;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QueryCristinPersonHandlerTest {

    private static final String RANDOM_NAME = "John Smith";
    private static final String NVA_API_QUERY_PERSON_JSON =
        "nvaApiQueryPersonResponse.json";
    private static final String PROBLEM_JSON = APPLICATION_PROBLEM_JSON.toString();
    private static final String ZERO_VALUE = "0";
    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String EMPTY_LIST_STRING = "[]";
    private static final String EXPECTED_CRISTIN_URI_WITH_PARAMS =
        "https://api.cristin-test.uio.no/v2/persons?per_page=5&name=John+Smith&page=1&lang=en,nb,nn";

    private CristinPersonApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private QueryCristinPersonHandler handler;
    private final List<String> generatedNINs = List.of("04031839594",
                                                       "23047748038",
                                                       "07021702867",
                                                       "04011679604",
                                                       "20106514977");

    @BeforeEach
    void setUp() {
        apiClient = new CristinPersonApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryCristinPersonHandler(apiClient, environment);
    }

    @Test
    void shouldReturnResponseWhenCallingEndpointWithNameParameter() throws IOException {
        SearchResponse<Person> actual = sendDefaultQuery().getBodyObject(SearchResponse.class);
        String expectedString = IoUtils.stringFromResources(Path.of(NVA_API_QUERY_PERSON_JSON));
        SearchResponse<Person> expected = OBJECT_MAPPER.readValue(expectedString, SearchResponse.class);

        // Type casting problems when using generic types. Needed to convert. Was somehow converting to LinkedHashMap
        List<Person> expectedPersons = OBJECT_MAPPER.convertValue(expected.getHits(), new TypeReference<>() {});
        List<Person> actualPersons = OBJECT_MAPPER.convertValue(actual.getHits(), new TypeReference<>() {});
        expected.setHits(expectedPersons);
        actual.setHits(actualPersons);

        assertEquals(expected, actual);
    }

    @Test
    void shouldHideInternalExceptionFromClientWhenUnknownErrorOccur() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        doThrow(new RuntimeException()).when(apiClient).getEnrichedPersonsUsingQueryResponse(any());
        handler = new QueryCristinPersonHandler(apiClient, environment);

        GatewayResponse<SearchResponse> gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void shouldReturnBadGatewayToClientWhenBackendFetchFails() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        HttpResponse<String> response = new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(response).when(apiClient).fetchQueryResults(any());
        handler = new QueryCristinPersonHandler(apiClient, environment);

        GatewayResponse<SearchResponse> gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void shouldReturnResponseFromQueryInsteadOfEnrichedGetWhenEnrichingFails() throws IOException {
        apiClient = spy(apiClient);
        HttpResponse<String> response =
            new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(CompletableFuture.completedFuture(response)).when(apiClient).fetchGetResultAsync(any());
        handler = new QueryCristinPersonHandler(apiClient, environment);
        SearchResponse searchResponse = sendDefaultQuery().getBodyObject(SearchResponse.class);

        // Query response has size of 2, and will return that even if trying to enrich those 2 returns empty
        assertThat(2, equalTo(searchResponse.getHits().size()));
    }

    @Test
    void shouldHaveCorsHeaderAllOriginOnResponse() throws Exception {
        var gatewayResponse = sendDefaultQuery();
        assertEquals(ALLOW_ALL_ORIGIN, gatewayResponse.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void shouldReturnSearchResponseWithEmptyHitsWhenBackendFetchIsEmpty() throws ApiGatewayException, IOException {
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK,
            generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE))).when(apiClient).queryPersons(any());
        doReturn(Collections.emptyList()).when(apiClient).fetchQueryResultsOneByOne(any());
        handler = new QueryCristinPersonHandler(apiClient, environment);
        SearchResponse<Person> searchResponse = sendDefaultQuery().getBodyObject(SearchResponse.class);

        assertThat(0, equalTo(searchResponse.getHits().size()));
    }

    @Test
    void shouldProduceCorrectCristinUriFromParams() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        handler = new QueryCristinPersonHandler(apiClient, environment);
        sendDefaultQuery();
        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_PARAMS).getUri());
    }

    @Test
    void shouldReturnGatewayTimeoutWhenQueryToUpstreamServerTimesOut() throws Exception {
        HttpClient clientMock = mock(HttpClient.class);
        when(clientMock.<String>send(any(), any())).thenThrow(new HttpConnectTimeoutException(EMPTY_STRING));
        CristinPersonApiClient apiClient = new CristinPersonApiClient(clientMock);
        handler = new QueryCristinPersonHandler(apiClient, environment);
        GatewayResponse<SearchResponse> gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_GATEWAY_TIMEOUT, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_GATEWAY_TIMEOUT));
    }

    @Test
    void shouldNotCallAuthorizedQueryMethodWhenClientDoesNotHaveRequiredRights()
        throws IOException, ApiGatewayException {
        var apiClient = mock(CristinPersonApiClient.class);
        var searchResponse = randomPersons();
        doReturn(searchResponse).when(apiClient).generateQueryResponse(any());
        handler = new QueryCristinPersonHandler(apiClient, new Environment());
        var input = queryMissingAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HttpURLConnection.HTTP_OK));
        verify(apiClient, times(0)).authorizedGenerateQueryResponse(any());
        verify(apiClient, times(1)).generateQueryResponse(any());
    }

    @Test
    void shouldCallAuthorizedQueryMethodWhenClientDoesHaveRequiredRights()
        throws IOException, ApiGatewayException {
        var apiClient = mock(CristinPersonApiClient.class);
        var searchResponse = randomPersons();
        doReturn(searchResponse).when(apiClient).authorizedGenerateQueryResponse(any());
        handler = new QueryCristinPersonHandler(apiClient, new Environment());
        var input = queryWithAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HttpURLConnection.HTTP_OK));
        verify(apiClient, times(1)).authorizedGenerateQueryResponse(any());
        verify(apiClient, times(0)).generateQueryResponse(any());
    }

    @Test
    void shouldIncludeNationalIdentificationNumberInResponseWhenPresentInUpstream()
        throws ApiGatewayException, IOException {
        var apiClient = spy(CristinPersonApiClient.class);
        var queryResponse = generateDummyQueryResponseWithNin();
        doReturn(queryResponse).when(apiClient).fetchGetResultWithAuthentication(any());
        var getResponse = generateDummyGetResponseWithNin();
        doReturn(List.of(getResponse)).when(apiClient).authorizedFetchQueryResultsOneByOne(any());
        handler = new QueryCristinPersonHandler(apiClient, new Environment());
        var input = queryWithAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        assertThat(gatewayResponse.getBody(), containsString(NATIONAL_IDENTITY_NUMBER));
    }

    @Test
    void shouldNotIncludeNationalIdentificationNumberInResponseWhenMissingFromUpstream()
        throws ApiGatewayException, IOException {
        var apiClient = spy(CristinPersonApiClient.class);
        var queryResponse = generateDummyQueryResponseWithoutNin();
        doReturn(queryResponse).when(apiClient).queryPersons(any());
        var getResponse = generateDummyGetResponseWithoutNin();
        doReturn(List.of(getResponse)).when(apiClient).fetchQueryResultsOneByOne(any());
        handler = new QueryCristinPersonHandler(apiClient, new Environment());
        var input = queryMissingAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        assertThat(gatewayResponse.getBody(), not(containsString(NATIONAL_IDENTITY_NUMBER)));
    }

    private SearchResponse<Person> randomPersonsWithNIN() {
        List<Person> persons = new ArrayList<>();
        IntStream.range(0, 5).mapToObj(i -> randomPersonWithNin()).forEach(persons::add);
        return new SearchResponse<Person>(randomUri()).withHits(persons);
    }

    private SearchResponse<Person> randomPersons() {
        var persons = new ArrayList<Person>();
        IntStream.range(0, 5).mapToObj(i -> randomPerson()).forEach(persons::add);
        return new SearchResponse<Person>(randomUri()).withHits(persons);
    }

    private Person randomPerson() {
        return new Person.Builder()
                   .withId(randomUri())
                   .build();
    }

    private Person randomPersonWithNin() {
        return new Person.Builder()
                   .withId(randomUri())
                   .withNorwegianNationalId(randomElement(generatedNINs))
                   .build();
    }

    private HttpResponse<String> generateDummyQueryResponseWithNin() throws JsonProcessingException {
        var cristinPersons = List.of(randomCristinPerson(randomElement(generatedNINs)));
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPersons), 200);
    }

    private HttpResponse<String> generateDummyGetResponseWithNin() throws JsonProcessingException {
        var cristinPerson = randomCristinPerson(randomElement(generatedNINs));
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPerson), 200);
    }

    private HttpResponse<String> generateDummyQueryResponseWithoutNin() throws JsonProcessingException {
        var cristinPersons = List.of(randomCristinPerson(null));
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPersons), 200);
    }

    private HttpResponse<String> generateDummyGetResponseWithoutNin() throws JsonProcessingException {
        var cristinPerson = randomCristinPerson(null);
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPerson), 200);
    }

    private CristinPerson randomCristinPerson(String nin) {
        var cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(randomString());
        cristinPerson.setFirstName(randomString());
        cristinPerson.setNorwegianNationalId(nin);
        return cristinPerson;
    }

    private InputStream queryWithAccessRightToReadNIN(Map<String, String> queryParameters)
        throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withQueryParameters(queryParameters)
                   .withCustomerId(customerId)
                   .withAccessRights(customerId, AccessRight.EDIT_OWN_INSTITUTION_USERS.toString())
                   .build();
    }

    private InputStream queryMissingAccessRightToReadNIN(Map<String, String> queryParameters)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withQueryParameters(queryParameters)
                   .build();
    }

    private GatewayResponse<SearchResponse> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

    private java.net.http.HttpHeaders generateHeaders(String totalCount, String link) {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(totalCount, link), HttpResponseFaker.filter());
    }
}
