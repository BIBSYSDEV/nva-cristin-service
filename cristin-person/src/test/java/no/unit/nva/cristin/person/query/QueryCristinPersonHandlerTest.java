package no.unit.nva.cristin.person.query;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.model.query.CristinFacetKey;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.query.version.facet.QueryPersonWithFacetsClient;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.hamcrest.CoreMatchers;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.INSTITUTION_PARAM;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.SECTOR_PARAM;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.VERIFIED;
import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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

    public static final String RESPONSE_WITH_FACETS = IoUtils.stringFromResources(
        Path.of("cristinQueryPersonDataAndFacets.json"));
    private static final String RANDOM_NAME = "John Smith";
    private static final String NVA_API_QUERY_PERSON_JSON =
        "nvaApiQueryPersonResponse.json";
    private static final String PROBLEM_JSON = APPLICATION_PROBLEM_JSON.toString();
    private static final String ZERO_VALUE = "0";
    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String EMPTY_LIST_STRING = "[]";
    private static final String EXPECTED_CRISTIN_URI_WITH_PARAMS =
        "https://api.cristin-test.uio.no/v2/persons?per_page=5&name=John%20Smith&page=1";
    private static final String EXPECTED_CRISTIN_URI_WITH_ADDITIONAL_PARAMS =
        "https://api.cristin-test.uio.no/v2/persons?per_page=5&institution=uio&name=John%20Smith&verified=true&page=1"
        + "&sort=name%20desc";
    private static final String ORGANIZATION_UIO = "uio";
    public static final String SECTOR_FACET_UC = "UC";
    public static final String INSTITUTION_FACET_185 = "185";
    public static final String SECTOR_INSTITUTE = "INSTITUTE";
    public static final String VERSION_2023_11_03_AGGREGATIONS = "application/json; version=2023-11-03-aggregations";
    public static final String VERSION_NAME_AGGREGATIONS = "application/json; version=aggregations";
    public static final String VERSION_DATE_AGGREGATIONS = "application/json; version=2023-11-03";
    public static final String EMPTY_OBJECT = "{}";
    public static final String DELIMITER = ",";
    public static final String EQUALS = "=";
    public static final String NAME_DESC = "name desc";
    public static final String ENCODED_NAME = "t%C3%B8rresen";
    public static final String ENCODED_ORGANIZATION = "H%C3%B8gskole";
    public static final String INVALID_ENCODING = "%EF%BF%BD";
    private static final String NAME_WITH_SPECIAL_CHARACTERS = "Jéan De'La #Luc";
    private static final String ORGANIZATION_WITH_SPECIAL_CHARACTERS = "Unévers De'La #Spec";

    private CristinPersonApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private QueryCristinPersonHandler handler;
    private final List<String> generatedNINs =
        List.of("04031839594", "23047748038", "07021702867", "04011679604", "20106514977");
    private DefaultPersonQueryClientProvider clientProvider;

    @BeforeEach
    void setUp() {
        clientProvider = new DefaultPersonQueryClientProvider();
        clientProvider = spy(clientProvider);
        apiClient = new CristinPersonApiClientStub();
        doReturn(apiClient).when(clientProvider).getVersionOne();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
    }

    @Test
    void shouldReturnResponseWhenCallingEndpointWithNameParameter() throws Exception {
        var actual = sendDefaultQuery().getBody();
        var expected = IoUtils.stringFromResources(Path.of(NVA_API_QUERY_PERSON_JSON));

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldHideInternalExceptionFromClientWhenUnknownErrorOccur() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        doThrow(new RuntimeException()).when(apiClient).getEnrichedPersonsUsingQueryResponse(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);

        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void shouldReturnBadGatewayToClientWhenBackendFetchFails() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        var response = new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(response).when(apiClient).fetchQueryResults(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);

        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void shouldReturnResponseFromQueryInsteadOfEnrichedGetWhenEnrichingFails() throws IOException {
        apiClient = spy(apiClient);
        var response = new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(CompletableFuture.completedFuture(response)).when(apiClient).fetchGetResultAsync(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        var searchResponse = sendDefaultQuery().getBodyObject(SearchResponse.class);

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
            generateHeaders())).when(apiClient).queryPersons(any());
        doReturn(Collections.emptyList()).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        var searchResponse = sendDefaultQuery().getBodyObject(SearchResponse.class);

        assertThat(0, equalTo(searchResponse.getHits().size()));
    }

    @Test
    void shouldProduceCorrectCristinUriFromParams() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        sendDefaultQuery();
        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_PARAMS).getUri());
    }

    @Test
    void shouldProduceCorrectCristinUriFromParamsWithAdditionalOptionalParams() throws IOException,
                                                                                       ApiGatewayException {
        apiClient = spy(apiClient);
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        sendQueryWithAdditionalParams();

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_ADDITIONAL_PARAMS).getUri());
    }

    @Test
    void shouldReturnHttpRequestFailedExceptionWhenQueryToUpstreamServerTimesOut() throws Exception {
        var clientMock = mock(HttpClient.class);
        when(clientMock.<String>send(any(), any())).thenThrow(new HttpConnectTimeoutException(EMPTY_STRING));
        CristinPersonApiClient apiClient = new CristinPersonApiClient(clientMock);
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void shouldNotCallAuthorizedQueryMethodWhenClientDoesNotHaveRequiredRights()
        throws IOException, ApiGatewayException {
        var apiClient = mock(CristinPersonApiClient.class);
        var searchResponse = randomPersons();
        doReturn(searchResponse).when(apiClient).generateQueryResponse(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var input = queryMissingAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HttpURLConnection.HTTP_OK));
        verify(apiClient, times(0)).executeAuthorizedQuery(any());
        verify(apiClient, times(1)).executeQuery(any());
    }

    @ParameterizedTest(name = "Query returns 200 OK when having access right: {0}")
    @MethodSource("accessRightProvider")
    void shouldCallAuthorizedQueryMethodWhenClientDoesHaveRequiredRights(AccessRight accessRight)
        throws IOException, ApiGatewayException {
        var apiClient = mock(CristinPersonApiClient.class);
        var searchResponse = randomPersons();
        doReturn(searchResponse).when(apiClient).executeAuthorizedQuery(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var input = queryWithAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME), accessRight);
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HttpURLConnection.HTTP_OK));
        verify(apiClient, times(1)).executeAuthorizedQuery(any());
        verify(apiClient, times(0)).executeQuery(any());
    }

    @ParameterizedTest(name = "Query returns authorized fields when having access right: {0}")
    @MethodSource("accessRightProvider")
    void shouldIncludeNationalIdentificationNumberInResponseWhenPresentInUpstream(AccessRight accessRight)
        throws ApiGatewayException, IOException {
        var apiClient = spy(CristinPersonApiClient.class);
        var queryResponse = generateDummyQueryResponseWithNin();
        doReturn(queryResponse).when(apiClient).fetchGetResultWithAuthentication(any());
        var getResponse = generateDummyGetResponseWithNin();
        doReturn(List.of(getResponse)).when(apiClient).authorizedFetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var input = queryWithAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME), accessRight);
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
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var input = queryMissingAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        assertThat(gatewayResponse.getBody(), not(containsString(NATIONAL_IDENTITY_NUMBER)));
    }

    @Test
    void shouldNotIncludeAuthorizedFieldsWhenUnauthorizedQueryEvenIfPresentInUpstream()
        throws ApiGatewayException, IOException {

        var apiClient = spy(CristinPersonApiClient.class);

        var queryResponse = generateDummyQueryResponseWithNin();
        doReturn(queryResponse).when(apiClient).fetchGetResultWithAuthentication(any());
        doReturn(queryResponse).when(apiClient).fetchGetResult(any());

        var getResponse = generateDummyGetResponseWithNin();
        doReturn(List.of(getResponse)).when(apiClient).authorizedFetchQueryResultsOneByOne(any());
        doReturn(List.of(getResponse)).when(apiClient).fetchQueryResultsOneByOne(any());

        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var input = queryMissingAccessRightToReadNIN(Map.of(NAME, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var responseBody = gatewayResponse.getBody();

        assertThat(responseBody, not(containsString(NATIONAL_IDENTITY_NUMBER)));
        assertThat(responseBody, not(containsString(RESERVED)));
        generatedNINs.forEach(nin -> assertThat(responseBody, not(containsString(nin))));
    }

    @ParameterizedTest
    @ValueSource(strings = {VERSION_DATE_AGGREGATIONS, VERSION_2023_11_03_AGGREGATIONS, VERSION_NAME_AGGREGATIONS})
    void shouldAddFacetsToSearchResponse(String acceptHeader) throws Exception {
        var apiClient = spy(QueryPersonWithFacetsClient.class);
        var queryResponse = dummyFacetHttpResponse();
        doReturn(queryResponse).when(apiClient).fetchQueryResults(any());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var isAnAuthorizedQuery = false;

        var actual = sendQueryWithFacets(isAnAuthorizedQuery, acceptHeader).getBodyObject(SearchResponse.class);

        assertThat(actual.getAggregations().size(), equalTo(2));
        assertThat(actual.getHits().size(), equalTo(2));
    }

    @Test
    void shouldAddFacetsToAuthorizedSearchResponse() throws Exception {
        var apiClient = spy(QueryPersonWithFacetsClient.class);
        var queryResponse = dummyFacetHttpResponse();
        doReturn(queryResponse).when(apiClient).fetchQueryResults(any());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var isAnAuthorizedQuery = true;

        var actual = sendQueryWithFacets(isAnAuthorizedQuery, VERSION_2023_11_03_AGGREGATIONS)
                         .getBodyObject(SearchResponse.class);

        verify(apiClient, times(1)).executeAuthorizedQuery(any());
        assertThat(actual.getAggregations().size(), equalTo(2));
        assertThat(actual.getHits().size(), equalTo(2));
    }

    @Test
    void shouldReturnEmptyResponseWhenEmptyUpstream() throws Exception {
        var apiClient = spy(QueryPersonWithFacetsClient.class);
        var queryResponse = new HttpResponseFaker(EMPTY_OBJECT, 200);
        doReturn(queryResponse).when(apiClient).fetchQueryResults(any());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var isAnAuthorizedQuery = false;

        var actual = sendQueryWithFacets(isAnAuthorizedQuery, VERSION_2023_11_03_AGGREGATIONS)
                         .getBodyObject(SearchResponse.class);

        assertThat(actual.getId(), not(nullValue()));
        assertThat(actual.getHits().size(), equalTo(0));
    }

    @Test
    void shouldAddOrganizationBothAsFacetAndAsRegularParamAndSupportCombiningThemAsCommaSeparated() throws Exception {
        var apiClient = spy(QueryPersonWithFacetsClient.class);
        var queryResponse = dummyFacetHttpResponse();
        doReturn(queryResponse).when(apiClient).fetchQueryResults(any());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinPersonHandler(clientProvider, new Environment());
        var isAnAuthorizedQuery = false;

        final var actual = sendQueryWithFacets(isAnAuthorizedQuery, VERSION_2023_11_03_AGGREGATIONS)
                         .getBodyObject(SearchResponse.class);

        var captor = ArgumentCaptor.forClass(URI.class);
        verify(apiClient).fetchQueryResults(captor.capture());

        assertThat(captor.getValue().toString(), containsString(INSTITUTION + EQUALS + ORGANIZATION_UIO));
        assertThat(captor.getValue().toString(), containsString(INSTITUTION + EQUALS + INSTITUTION_FACET_185));
        assertThat(actual.getId().toString(),
                   containsString(ORGANIZATION + EQUALS + ORGANIZATION_UIO));
        assertThat(actual.getId().toString(),
                   containsString(CristinFacetKey.INSTITUTION.getNvaKey() + EQUALS + INSTITUTION_FACET_185));
    }

    @Test
    void shouldReturnAllResultsWhenCallingEndpointWithNoParameters() throws IOException {
        var input = new HandlerRequestBuilder<>(OBJECT_MAPPER)
                        .withBody(null)
                        .build();
        handler.handleRequest(input, output, context);

        var actual = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertThat(actual.getStatusCode(), equalTo(HttpURLConnection.HTTP_OK));
    }

    @Test
    void shouldAvoidDoubleEncodingWhenInputFromClientIsAlreadyEncoded() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        try (var input = requestWithQueryParameters(Map.of(NAME, ENCODED_NAME,
                                                           ORGANIZATION, ENCODED_ORGANIZATION))) {
            handler.handleRequest(input, output, context);
        }

        GatewayResponse.fromOutputStream(output, SearchResponse.class);

        var captor = ArgumentCaptor.forClass(URI.class);
        verify(apiClient).fetchQueryResults(captor.capture());

        assertThat(captor.getValue().toString(), containsString(ENCODED_NAME));
        assertThat(captor.getValue().toString(), containsString(ENCODED_ORGANIZATION));
    }

    @Test
    void shouldAvoidDoubleEncodingInVersionWithFacetsUsingFacetsWhenInputFromClientIsAlreadyEncoded()
        throws Exception {

        var apiClient = spy(ApiClientFacetsStub.class);
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinPersonHandler(clientProvider, environment);
        var request = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                          .withBody(null)
                          .withQueryParameters(Map.of(NAME, ENCODED_NAME,
                                                      ORGANIZATION, ENCODED_ORGANIZATION,
                                                      SECTOR_PARAM.getNvaKey(), multiValuedFacetParam()))
                          .withHeaders(Map.of(ACCEPT, VERSION_2023_11_03_AGGREGATIONS))
                          .build();

        handler.handleRequest(request, output, context);

        assertThat(apiClient.getHttpRequest().uri().toString(), not(containsString(INVALID_ENCODING)));
    }

    @Test
    void shouldAllowSpecialCharactersForQueryString() throws Exception {
        var input = requestWithQueryParameters(Map.of(NAME, NAME_WITH_SPECIAL_CHARACTERS,
                                                      ORGANIZATION, ORGANIZATION_WITH_SPECIAL_CHARACTERS));
        handler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var searchString = gatewayResponse.getBodyObject(SearchResponse.class).getSearchString();

        assertThat(searchString, containsString(NAME_WITH_SPECIAL_CHARACTERS));
        assertThat(searchString, containsString(ORGANIZATION_WITH_SPECIAL_CHARACTERS));
        assertThat(gatewayResponse.getStatusCode(), CoreMatchers.equalTo(HTTP_OK));
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

    private HttpResponse<String> generateDummyQueryResponseWithNin() throws JsonProcessingException {
        var cristinPersons = List.of(randomCristinPersonWithNin(randomElement(generatedNINs)));
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPersons), 200);
    }

    private HttpResponse<String> generateDummyGetResponseWithNin() throws JsonProcessingException {
        var cristinPerson = randomCristinPersonWithNin(randomElement(generatedNINs));
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPerson), 200);
    }

    private HttpResponse<String> generateDummyQueryResponseWithoutNin() throws JsonProcessingException {
        var cristinPersons = List.of(randomCristinPerson());
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPersons), 200);
    }

    private HttpResponse<String> generateDummyGetResponseWithoutNin() throws JsonProcessingException {
        var cristinPerson = randomCristinPerson();
        return new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinPerson), 200);
    }

    private CristinPerson randomCristinPerson() {
        var cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(randomString());
        cristinPerson.setFirstName(randomString());
        return cristinPerson;
    }

    private CristinPerson randomCristinPersonWithNin(String nin) {
        var cristinPerson = randomCristinPerson();
        cristinPerson.setNorwegianNationalId(nin);
        cristinPerson.setReserved(true);
        return cristinPerson;
    }

    private InputStream queryWithAccessRightToReadNIN(Map<String, String> queryParameters,
                                                      AccessRight accessRight)
        throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withQueryParameters(queryParameters)
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, accessRight)
                   .build();
    }

    private InputStream queryMissingAccessRightToReadNIN(Map<String, String> queryParameters)
        throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withQueryParameters(queryParameters)
                   .build();
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendDefaultQuery() throws IOException {
        try (var input = requestWithQueryParameters(Map.of(NAME, RANDOM_NAME))) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private void sendQueryWithAdditionalParams() throws IOException {
        try (var input = requestWithQueryParameters(Map.of(NAME, RANDOM_NAME,
                                                           ORGANIZATION, ORGANIZATION_UIO,
                                                           VERIFIED, Boolean.TRUE.toString(),
                                                           SORT, NAME_DESC))) {
            handler.handleRequest(input, output, context);
        }

        GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

    private java.net.http.HttpHeaders generateHeaders() {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(ZERO_VALUE, LINK_EXAMPLE_VALUE),
                                            HttpResponseFaker.filter());
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendQueryWithFacets(boolean authorized, String acceptValue)
        throws IOException {

        var params = Map.of(NAME, RANDOM_NAME,
                            SECTOR_PARAM.getNvaKey(), multiValuedFacetParam(),
                            INSTITUTION_PARAM.getNvaKey(), INSTITUTION_FACET_185,
                            ORGANIZATION, ORGANIZATION_UIO);
        var acceptHeader = Map.of(ACCEPT, acceptValue);

        var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                        .withBody(null)
                        .withQueryParameters(params)
                        .withHeaders(acceptHeader);

        if (authorized) {
            var customerId = randomUri();
            input.withCurrentCustomer(customerId)
                .withAccessRights(customerId, MANAGE_OWN_AFFILIATION);
        }

        handler.handleRequest(input.build(), output, context);

        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private static String multiValuedFacetParam() {
        return String.join(DELIMITER, SECTOR_FACET_UC, SECTOR_INSTITUTE);
    }

    private HttpResponse<String> dummyFacetHttpResponse() {
        return new HttpResponseFaker(RESPONSE_WITH_FACETS, 200);
    }

    private static Stream<Arguments> accessRightProvider() {
        return Stream.of(Arguments.of(MANAGE_OWN_AFFILIATION),
                         Arguments.of(MANAGE_CUSTOMERS));
    }

    static class ApiClientFacetsStub extends QueryPersonWithFacetsClient {
        private HttpRequest httpRequest;

        @Override
        protected HttpResponse<String> getSuccessfulResponseOrThrowException(HttpRequest httpRequest)
            throws FailedHttpRequestException {
            this.httpRequest = httpRequest;

            return super.getSuccessfulResponseOrThrowException(httpRequest);
        }

        public HttpRequest getHttpRequest() {
            return httpRequest;
        }
    }

}
