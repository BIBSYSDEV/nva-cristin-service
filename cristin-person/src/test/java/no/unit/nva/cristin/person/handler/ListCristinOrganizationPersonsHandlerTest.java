package no.unit.nva.cristin.person.handler;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.person.handler.ListCristinOrganizationPersonsHandler.VALID_QUERY_PARAMETERS;
import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Comparators;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinOrganizationPersonsClient;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.problem.Problem;

class ListCristinOrganizationPersonsHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String DUMMY_ORGANIZATION_IDENTIFIER = "4.3.2.1";
    public static final String SAMPLE_PAGE = "2";
    public static final String SAMPLE_RESULTS_SIZE = "10";
    public static final String INVALID_KEY = "invalid";
    public static final String INVALID_VALUE = "value";
    private static final String EMPTY_LIST_STRING = "[]";
    private static final String ZERO_VALUE = "0";
    public static final String EQUAL_OPERATOR = "=";

    private ListCristinOrganizationPersonsHandler handler;
    private ByteArrayOutputStream output;
    private Context context;
    private CristinOrganizationPersonsClient cristinApiClient;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        cristinApiClient = new CristinOrganizationPersonsClient();
        output = new ByteArrayOutputStream();
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        InputStream inputStream = generateHandlerRequestWithoutOrganizationIdentifier();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnInvalidQueryParameters() throws IOException {
        InputStream inputStream = generateHandlerDummyRequestWithIllegalQueryParameters();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS)));
    }

    @Test
    void shouldReturnOKAndEmptyResponseOnValidDummyInput() throws IOException {
        InputStream inputStream = generateHandlerDummyRequest();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnOKAndIdInResponseOnValidDummyInput() throws IOException {
        InputStream inputStream = generateHandlerDummyRequest();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        final SearchResponse searchResponse = gatewayResponse.getBodyObject(SearchResponse.class);
        assertTrue(searchResponse.getId().toString().contains(DUMMY_ORGANIZATION_IDENTIFIER));
    }

    @Test
    void shouldReturnSearchResponseWithEmptyHitsWhenBackendFetchIsEmpty() throws ApiGatewayException, IOException {

        CristinOrganizationPersonsClient apiClient = spy(cristinApiClient);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK,
                                       generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE))).when(apiClient)
            .queryPersons(any());
        doReturn(Collections.emptyList()).when(apiClient).fetchQueryResultsOneByOne(any());
        handler = new ListCristinOrganizationPersonsHandler(apiClient, new Environment());
        handler.handleRequest(generateHandlerDummyRequest(), output, context);
        GatewayResponse<SearchResponse> response = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        SearchResponse<Person> searchResponse = response.getBodyObject(SearchResponse.class);
        assertThat(0, equalTo(searchResponse.getHits().size()));
    }

    @Test
    void shouldAddNameParamToCristinQueryForFilteringOnNameAndReturnOk() throws IOException, ApiGatewayException {
        cristinApiClient = spy(cristinApiClient);
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
        var name = randomString();
        var input = queryWithNameParam(name);
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        var responseBody = gatewayResponse.getBodyObject(SearchResponse.class);
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClient).fetchQueryResults(captor.capture());
        assertThat(Optional.ofNullable(captor.getValue()).toString(),
                   containsString(NAME + EQUAL_OPERATOR + name));
        assertThat(Optional.ofNullable(responseBody.getId()).toString(),
                   containsString(NAME + EQUAL_OPERATOR + name));
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldSortResultsBasedOnPersonIdentifier() throws IOException, ApiGatewayException {
        var fakeHttpResponse = generateCristinResponseWithDifferentIdentifiers();
        cristinApiClient = spy(cristinApiClient);
        doReturn(fakeHttpResponse).when(cristinApiClient).fetchQueryResults(any());
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
        var input = generateHandlerDummyRequest();
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        var responseBody = gatewayResponse.getBodyObject(SearchResponse.class);
        var hits = extractHitsFromSearchResponse(responseBody);
        var isOrdered = Comparators.isInOrder(hits, compareCristinIdentifiersDesc());

        assertThat(isOrdered, equalTo(true));
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    private HttpResponseFaker generateCristinResponseWithDifferentIdentifiers() throws JsonProcessingException {
        var cristinResponse = restApiMapper.createArrayNode();
        cristinResponse.addPOJO(generateCristinPerson(20, randomString()));
        cristinResponse.addPOJO(generateCristinPerson(30, randomString()));
        cristinResponse.addPOJO(generateCristinPerson(10, randomString()));
        return new HttpResponseFaker(restApiMapper.writeValueAsString(cristinResponse), 200);
    }

    private Comparator<Person> compareCristinIdentifiersDesc() {
        return Comparator.comparing(this::extractCristinIdentifierFromPerson).reversed();
    }

    private Integer extractCristinIdentifierFromPerson(Person person) {
        return Optional.ofNullable(person)
                   .map(Person::getId)
                   .map(UriUtils::extractLastPathElement)
                   .map(Integer::valueOf)
                   .orElseThrow();
    }

    private CristinPerson generateCristinPerson(int identifier, String name) {
        var cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(String.valueOf(identifier));
        cristinPerson.setFirstName(name);
        return cristinPerson;
    }

    private List<Person> extractHitsFromSearchResponse(SearchResponse response) {
        return OBJECT_MAPPER.convertValue(response.getHits(), new TypeReference<>() {
        });
    }

    private InputStream queryWithNameParam(String name) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(restApiMapper)
                   .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                   .withQueryParameters(Map.of(NAME, name))
                   .build();
    }

    private InputStream generateHandlerDummyRequestWithIllegalQueryParameters() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                   .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                   .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                   .withQueryParameters(Map.of(INVALID_KEY, INVALID_VALUE))
                   .build();
    }

    private InputStream generateHandlerDummyRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                .withQueryParameters(Map.of(PAGE, SAMPLE_PAGE))
                .withQueryParameters(Map.of(NUMBER_OF_RESULTS, SAMPLE_RESULTS_SIZE))
                .build();
    }

    private InputStream generateHandlerRequestWithoutOrganizationIdentifier() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

    private java.net.http.HttpHeaders generateHeaders(String totalCount, String link) {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(totalCount, link), HttpResponseFaker.filter());
    }

}
