package no.unit.nva.cristin.person.query.organization;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.EQUAL_OPERATOR;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.person.query.organization.ListCristinOrganizationPersonsHandler.VALID_QUERY_PARAMETERS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ListCristinOrganizationPersonsHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String DUMMY_ORGANIZATION_IDENTIFIER = "4.3.2.1";
    public static final String SAMPLE_PAGE = "2";
    public static final String SAMPLE_RESULTS_SIZE = "10";
    public static final String INVALID_KEY = "invalid";
    public static final String INVALID_VALUE = "value";
    public static final int PERSON_COUNT = 5;
    private static final String EMPTY_LIST_STRING = "[]";

    public static final String SORT_VALUE = "id desc";

    private ListCristinOrganizationPersonsHandler handler;
    private ByteArrayOutputStream output;
    private Context context;
    private CristinOrganizationPersonsClient cristinApiClient;
    private final List<String> generatedNINs = List.of("04031839594",
                                                       "23047748038",
                                                       "07021702867",
                                                       "04011679604",
                                                       "20106514977");

    @BeforeEach
    void setUp() throws Exception {
        context = mock(Context.class);
        var mockHttpClient = mock(HttpClient.class);
        mockHttpClient = spy(mockHttpClient);

        HttpResponse<String> fakeResponse = new HttpResponseFaker(EMPTY_LIST_STRING, 200);
        doReturn(fakeResponse).when(mockHttpClient).send(any(), any());
        var fakeAsyncResponse = CompletableFuture.completedFuture(fakeResponse);
        doReturn(fakeAsyncResponse).when(mockHttpClient).sendAsync(any(), any());

        cristinApiClient = new CristinOrganizationPersonsClient(mockHttpClient);
        output = new ByteArrayOutputStream();
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        try (var inputStream = generateHandlerRequestWithoutOrganizationIdentifier()) {
            handler.handleRequest(inputStream, output, context);
        }
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnInvalidQueryParameters() throws IOException {
        try (var inputStream = generateHandlerDummyRequestWithIllegalQueryParameters()) {
            handler.handleRequest(inputStream, output, context);
        }
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS)));
    }

    @Test
    void shouldReturnOkAndSearchResponseWithEmptyHitsWhenBackendFetchDefaultsToEmpty() throws IOException {
        handler.handleRequest(generateHandlerDummyRequest(), output, context);
        var response = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var responseBody = response.getBodyObject(SearchResponse.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(0, equalTo(responseBody.getHits().size()));
        assertTrue(responseBody.getId().toString().contains(DUMMY_ORGANIZATION_IDENTIFIER));
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
    void shouldAddSortParamToCristinQueryForSortingHitsAndReturnOk() throws IOException, ApiGatewayException {
        cristinApiClient = spy(cristinApiClient);
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
        var input = queryWithSortParam();
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        var responseBody = gatewayResponse.getBodyObject(SearchResponse.class);
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClient).fetchQueryResults(captor.capture());
        assertThat(Optional.ofNullable(captor.getValue()).toString(),
                   containsString(SORT + EQUAL_OPERATOR + UriUtils.escapeWhiteSpace(SORT_VALUE)));
        assertThat(Optional.ofNullable(responseBody.getId()).toString(),
                   containsString(SORT + EQUAL_OPERATOR + UriUtils.escapeWhiteSpace(SORT_VALUE)));
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }


    @Test
    void shouldIncludeNationalIdentificationNumberInResponseForAuthenticatedUser()
            throws IOException, ApiGatewayException {
        CristinOrganizationPersonsClient apiClient = mock(CristinOrganizationPersonsClient.class);
        SearchResponse<Person> searchResponse = randomPersonsWithNIN();
        doReturn(searchResponse).when(apiClient).authorizedGenerateQueryResponse(any());

        handler = new ListCristinOrganizationPersonsHandler(apiClient, new Environment());
        var input = queryWithAccessRightToReadNIN();
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                SearchResponse.class);
        assertThat(gatewayResponse.getBody(), containsString(NATIONAL_IDENTITY_NUMBER));
    }

    @Test
    void shouldNotIncludeNationalIdentificationNumberInResponseForUserWithoutAuthentication()
            throws IOException, ApiGatewayException {
        CristinOrganizationPersonsClient apiClient = mock(CristinOrganizationPersonsClient.class);
        SearchResponse<Person> searchResponse = randomPersons();
        doReturn(searchResponse).when(apiClient).generateQueryResponse(any());
        handler = new ListCristinOrganizationPersonsHandler(apiClient, new Environment());
        var input = queryMissingAccessRightToReadNIN();
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                SearchResponse.class);
        assertThat(gatewayResponse.getBody(), not(containsString(NATIONAL_IDENTITY_NUMBER)));
    }

    @Test
    void shouldNotDoAuthorizedQueriesWhenMissingAccessRight() throws Exception {
        cristinApiClient = spy(cristinApiClient);
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
        var input = queryMissingAccessRightToReadNIN();
        handler.handleRequest(input, output, context);

        verify(cristinApiClient, times(0)).authorizedFetchQueryResultsOneByOne(any());
        verify(cristinApiClient, times(0)).fetchGetResultWithAuthentication(any());
        verify(cristinApiClient, times(0)).authenticatedFetchGetResultAsync(any());
        verify(cristinApiClient, times(0)).authorizedGenerateQueryResponse(any());
        verify(cristinApiClient, times(1)).generateQueryResponse(any());
    }

    @Test
    void shouldDoAuthorizedQueriesWhenHavingRequiredAccessRight() throws Exception {
        cristinApiClient = spy(cristinApiClient);
        HttpResponse<String> fakeResponse = new HttpResponseFaker(EMPTY_LIST_STRING, 200);
        doReturn(fakeResponse).when(cristinApiClient).fetchGetResultWithAuthentication(any());
        handler = new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
        var input = queryWithAccessRightToReadNIN();
        handler.handleRequest(input, output, context);

        verify(cristinApiClient, times(1)).fetchGetResultWithAuthentication(any());
        verify(cristinApiClient, times(1)).authorizedGenerateQueryResponse(any());
        verify(cristinApiClient, times(0)).generateQueryResponse(any());
    }

    private SearchResponse<Person> randomPersonsWithNIN() {
        List<Person> persons = new ArrayList<>();
        IntStream.range(0, PERSON_COUNT).mapToObj(i -> randomPersonWithNin()).forEach(persons::add);
        return new SearchResponse<Person>(randomUri()).withHits(persons);
    }

    private SearchResponse<Person> randomPersons() {
        List<Person> persons = new ArrayList<>();
        IntStream.range(0, PERSON_COUNT).mapToObj(i -> randomPerson()).forEach(persons::add);
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
                   .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, randomElement(generatedNINs))))
                   .build();
    }

    private InputStream queryWithAccessRightToReadNIN() throws JsonProcessingException {
        final URI customerId = randomUri();
        return new HandlerRequestBuilder<Void>(restApiMapper)
                   .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, AccessRight.EDIT_OWN_INSTITUTION_USERS.toString())
                   .build();
    }

    private InputStream queryMissingAccessRightToReadNIN() throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(restApiMapper)
                .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                .build();
    }



    private InputStream queryWithNameParam(String name) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(restApiMapper)
                   .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                   .withQueryParameters(Map.of(NAME, name))
                   .build();
    }

    private InputStream queryWithSortParam() throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(restApiMapper)
                   .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                   .withQueryParameters(Map.of(SORT, SORT_VALUE))
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

}
