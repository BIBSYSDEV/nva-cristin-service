package no.unit.nva.cristin.projects.query;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_VALUE;
import static no.unit.nva.cristin.common.ErrorMessages.UPSTREAM_RETURNED_BAD_REQUEST;
import static no.unit.nva.cristin.model.Constants.CATEGORY_PARAM;
import static no.unit.nva.cristin.model.Constants.EQUAL_OPERATOR;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PROJECT_CREATOR_PARAM;
import static no.unit.nva.cristin.model.JsonPropertyNames.BIOBANK_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_KEYWORD;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_UNIT;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.PARTICIPANT_PARAM;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.CATEGORY_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.COORDINATING_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.FUNDING_SOURCE_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.HEALTH_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.ORGANIZATION;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PARTICIPANT_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PARTICIPATING_PERSON_ORG_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.QUERY;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.RESPONSIBLE_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.SECTOR_FACET;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.TITLE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.VALID_QUERY_PARAMETER_NVA_KEYS;
import static no.unit.nva.cristin.projects.query.QueryCristinProjectClientStub.CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE;
import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static no.unit.nva.cristin.testing.HttpResponseFaker.TOTAL_COUNT_EXAMPLE_VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.model.JsonPropertyNames;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.model.query.CristinFacetParamKey;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import no.unit.nva.cristin.projects.query.version.facet.QueryProjectWithFacetsClient;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.zalando.problem.Problem;



class QueryCristinProjectHandlerTest {

    public static final String LANGUAGE_NB = "nb";
    public static final String RANDOM_TITLE = "reindeer";
    public static final String ZERO_VALUE = "0";
    public static final String TOTAL_COUNT_EXAMPLE_250 = "250";
    public static final String PAGE_15 = "15";
    public static final String GRANT_ID_EXAMPLE = "1234567";
    public static final String WHITESPACE = " ";
    public static final String URI_WITH_ESCAPED_WHITESPACE =
        "https://api.dev.nva.aws.unit.no/cristin/project?page=1&results=5&title=reindeer%20reindeer";
    public static final String INVALID_QUERY_PARAM_KEY = "invalid";
    public static final String INVALID_QUERY_PARAM_VALUE = "value";
    public static final String PROBLEM_JSON = APPLICATION_PROBLEM_JSON.toString();
    public static final String MEDIATYPE_JSON_UTF8 = MediaType.JSON_UTF_8.toString();
    public static final String ILLEGAL_PROJECT_STATUS = "snart ferdig";
    private static final String INVALID_LANGUAGE = "ru";
    private static final String TITLE_ILLEGAL_CHARACTERS = "<script>Hallo</script>";
    private static final String INVALID_JSON = "This is not valid JSON!";
    private static final String EMPTY_LIST_STRING = "[]";
    private static final String SECOND_PAGE = "2";
    private static final String TEN_RESULTS = "10";
    private static final String URI_WITH_PAGE_NUMBER_VALUE_OF_TWO =
        "https://api.dev.nva.aws.unit.no/cristin/project?page=2&results=5&title=reindeer";
    private static final String URI_WITH_TEN_NUMBER_OF_RESULTS =
        "https://api.dev.nva.aws.unit.no/cristin/project?page=1&results=10&title=reindeer";
    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String API_QUERY_RESPONSE_NO_PROJECTS_FOUND_JSON =
        "nvaApiGetQueryResponseNoProjectsFound.json";
    private static final String SAMPLE_NVA_ORGANIZATION =
        "https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0";
    private static final String SAMPLE_NVA_ORGANIZATION_ENCODED =
        URLEncoder.encode(SAMPLE_NVA_ORGANIZATION, StandardCharsets.UTF_8);
    private static final String ILLEGAL_NVA_ORGANIZATION =
        "hps:/api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0";
    private static final String ILLEGAL_NVA_ORGANIZATION_ENCODED =
        URLEncoder.encode(ILLEGAL_NVA_ORGANIZATION, StandardCharsets.UTF_8);
    private static final String API_QUERY_RESPONSE_JSON =
        IoUtils.stringFromResources(Path.of("nvaApiGetQueryResponse.json"));
    public static final String FUNDING_SAMPLE = "NRE:1234";
    public static final String FUNDING_SAMPLE_ENCODED = "NRE%3A1234";
    public static final String BIOBANK_SAMPLE = String.valueOf(randomInteger());
    public static final String KEYWORD_SAMPLE = randomString();
    public static final String UNIT_ID_SAMPLE = "184.12.60.0";
    public static final String START_DATE = "start_date";
    public static final String BAD_PARAM_FOR_SORT = "cristin id";
    public static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON = "cristinQueryProjectsResponse.json";
    public static final String CRISTIN_GET_PROJECT_RESPONSE_JSON = "cristinGetProjectResponse.json";
    public static final String CREATOR_IDENTIFIER = "12345";
    public static final String VERSION_2023_11_03_AGGREGATIONS = "application/json; version=2023-11-03-aggregations";
    public static final String VERSION_NAME_AGGREGATIONS = "application/json; version=aggregations";
    public static final String VERSION_DATE_AGGREGATIONS = "application/json; version=2023-11-03";
    public static final String RESPONSE_WITH_FACETS = IoUtils.stringFromResources(
        Path.of("cristinQueryProjectDataAndFacets.json"));
    public static final String DOUBLE_ENCODED_COMMA_DELIMITER = "%252C";

    private final Environment environment = new Environment();
    private QueryCristinProjectApiClient cristinApiClientStub;
    private Context context;
    private ByteArrayOutputStream output;
    private QueryCristinProjectHandler handler;
    private DefaultProjectQueryClientProvider clientProvider;

    @BeforeEach
    void setUp() {
        clientProvider = new DefaultProjectQueryClientProvider();
        clientProvider = spy(clientProvider);
        cristinApiClientStub = new QueryCristinProjectClientStub();
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
    }

    @Test
    void handlerReturnsExpectedBodyWhenRequestInputIsValid() throws Exception {
        var actual = sendDefaultQuery().getBody();

        JSONAssert.assertEquals(API_QUERY_RESPONSE_JSON, actual, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void handlerReturnsOkWhenInputContainsTitleAndLanguage() throws Exception {
        var response = sendDefaultQuery();
        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @Test
    void handlerThrowsInternalErrorWhenQueryingProjectsFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(RuntimeException.class).when(cristinApiClientStub)
            .getEnrichedProjectsUsingQueryResponse(any());
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);

        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerReturnsNoResultsWhenEnrichingFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        var response = new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(CompletableFuture.completedFuture(response))
            .when(cristinApiClientStub).fetchGetResultAsync(any());
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
        var gatewayResponse = sendDefaultQuery();
        var actual = gatewayResponse.getBodyObject(SearchResponse.class);

        assertEquals(0, actual.getHits().size());
    }

    @Test
    void handlerReturnsOkWhenTitleContainsAeOeAacolon() throws Exception {
        final var map = Map.of(
            QUERY.getNvaKey(), RANDOM_TITLE + " æØÆØÅå: " + RANDOM_TITLE,
            ORGANIZATION.getNvaKey(),"https%3A%2F%2Fapi.dev.nva.aws.unit.no%2Fcristin%2Forganization%2F20202.0.0.0"
        );
        try (var input = requestWithQueryParameters(map)) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Object.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsOkWhenTitleContainsgjennomgaatt() throws Exception {
        try (var input = requestWithQueryParameters(
            Map.of(JsonPropertyNames.QUERY, "gjennomgått akutt"))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Object.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }



    @Test
    void handlerSetsDefaultValueForMissingOptionalLanguageParameterAndReturnOk() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, RANDOM_TITLE))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Object.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReceivesAllowOriginHeaderValueFromEnvironmentAndPutsItOnResponse() throws Exception {
        var gatewayResponse = sendDefaultQuery();
        assertEquals(ALLOW_ALL_ORIGIN, gatewayResponse.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void handlerReturnsBadRequestWhenTitleQueryParamIsEmpty() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(QUERY.getNvaKey(), EMPTY_STRING))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                   containsString(String.format(ERROR_MESSAGE_INVALID_VALUE, QUERY.getNvaKey())));
    }

    @Test
    void handlerReturnsOkWhenReceivingInvalidLanguageQueryParam() throws Exception {
        try (var input = requestWithQueryParameters(
            Map.of(JsonPropertyNames.QUERY, RANDOM_TITLE,
                   JsonPropertyNames.LANGUAGE, INVALID_LANGUAGE))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void readerThrowsIoExceptionWhenReadingInvalidJson() {
        Executable action = () -> ApiClient.fromJson(INVALID_JSON, CristinProject.class);
        assertThrows(IOException.class, action);
    }

    @Test
    void handlerReturnsProjectsWrapperWithAllMetadataButEmptyHitsArrayWhenNoMatchesAreFoundInCristin()
        throws Exception {

        fakeAnEmptyResponseFromQueryAndEnrichment();
        var expected = getBodyFromResource(API_QUERY_RESPONSE_NO_PROJECTS_FOUND_JSON);
        var actual = sendDefaultQuery().getBody();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class)
            .when(cristinApiClientStub).fetchQueryResults(any(URI.class));
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenThereIsThrownIoExceptionWhenReadingFromJson() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(EMPTY_STRING))
            .when(cristinApiClientStub).fetchQueryResults(any(URI.class));
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsPageParameter() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, SECOND_PAGE))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(URI_WITH_PAGE_NUMBER_VALUE_OF_TWO));
    }

    @Test
    void handlerThrowsBadRequestWhenQueryHasInvalidPageParameter() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, TITLE_ILLEGAL_CHARACTERS))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(),
                   containsString(String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE, JsonPropertyNames.PAGE)));
    }

    @ParameterizedTest(name = "Handler returns firstRecord {0} when record has pagination {1} and is on page {2}")
    @CsvSource({"1,200,1", "11,5,3", "226,25,10", "55,9,7"})
    void handlerReturnsProjectWrapperWithFirstRecordWhenInputIsIncludesCurrentPageAndNumberOfResults(int expected,
                                                                                                     String perPage,
                                                                                                     String currentPage)
        throws IOException, ApiGatewayException {

        modifyQueryResponseToClient(
            getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE),
            generateHeaders(TOTAL_COUNT_EXAMPLE_250, LINK_EXAMPLE_VALUE));

        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, currentPage,
            JsonPropertyNames.NUMBER_OF_RESULTS, perPage
        ))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var actual = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SearchResponse.class)
                             .getFirstRecord();
        assertEquals(expected, actual);
    }

    @Test
    void handlerThrowsBadRequestWhenPaginationExceedsNumberOfResults() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, PAGE_15,
            JsonPropertyNames.NUMBER_OF_RESULTS, TEN_RESULTS
        ))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(),
                   containsString(String.format(ErrorMessages.ERROR_MESSAGE_PAGE_OUT_OF_SCOPE,
                                                TOTAL_COUNT_EXAMPLE_VALUE)));
    }

    @Test
    void handlerThrowsBadRequestWhenRequestingPaginationOnQueryWithZeroResults() throws Exception {
        modifyQueryResponseToClient(
            getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE),
            generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE));

        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, SECOND_PAGE,
            JsonPropertyNames.NUMBER_OF_RESULTS, TEN_RESULTS
        ))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(),
                   containsString(String.format(ErrorMessages.ERROR_MESSAGE_PAGE_OUT_OF_SCOPE, ZERO_VALUE)));
    }

    @ParameterizedTest(
        name = "Handler throws bad request when supplying non positive integer for results {0} or page {1}")
    @CsvSource({"0,5", "5,0"})
    void handlerThrowsBadRequestWhenSuppliedWithNonPositiveIntegerForPageOrResults(String perPage, String currentPage)
        throws Exception {

        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, currentPage,
            JsonPropertyNames.NUMBER_OF_RESULTS, perPage
        ))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(), anyOf(
            containsString(
                String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE, JsonPropertyNames.NUMBER_OF_RESULTS)),
            containsString(String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE, JsonPropertyNames.PAGE)))
        );
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsResultsParameter() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.NUMBER_OF_RESULTS, TEN_RESULTS))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(URI_WITH_TEN_NUMBER_OF_RESULTS));
    }

    @Test
    void handlerThrowsBadRequestWhenQueryHasInvalidResultsParameter() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.NUMBER_OF_RESULTS, TITLE_ILLEGAL_CHARACTERS))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(),
                   containsString(String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE,
                                                JsonPropertyNames.NUMBER_OF_RESULTS)));
    }

    @ParameterizedTest(
        name = "Test using link {0} to assert next {1} and previous {2} using page {3} with {4} per page")
    @MethodSource("provideDifferentPaginationValuesAndAssertNextAndPreviousResultsIsCorrect")
    void handlerReturnsProjectsWrapperWithCorrectNextResultsAndPreviousResultsWhenLinkHeaderHasRelNextAndPrev(
        String link, String expectedNext, String expectedPrevious, String currentPage, String perPage)
        throws Exception {

        modifyQueryResponseToClient(
            getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE),
            generateHeaders(TOTAL_COUNT_EXAMPLE_250, link));

        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.PAGE, currentPage,
            JsonPropertyNames.NUMBER_OF_RESULTS, perPage
        ))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        var actualNext =
            Optional.ofNullable(OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SearchResponse.class)
                                    .getNextResults()).orElse(new URI(EMPTY_STRING)).toString();
        assertEquals(expectedNext, actualNext);

        var actualPrevious =
            Optional.ofNullable(OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SearchResponse.class)
                                    .getPreviousResults()).orElse(new URI(EMPTY_STRING)).toString();
        assertEquals(expectedPrevious, actualPrevious);
    }

    @Test
    void handlerReturnsMatchingProjectsFromGrantIdSearchWhenSuppliedWithOnlyNumber() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(
            getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE)))
            .when(cristinApiClientStub).queryProjects(any());

        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);

        try (var input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, GRANT_ID_EXAMPLE))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsProjectsFromTitleSearchWhenSuppliedWithQueryStringIncludingNumber() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(
            getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE)))
            .when(cristinApiClientStub).queryProjects(any());

        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);

        try (var input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY,
                                                                   GRANT_ID_EXAMPLE + RANDOM_TITLE))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsProjectsFromTitleSearchWhenGrantIdSearchReturnsZeroResults() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(
            getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE)))
            .when(cristinApiClientStub).queryProjects(any());

        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);

        try (var input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, GRANT_ID_EXAMPLE))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        var actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(5, actual.getHits().size());
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsTitleWithWhitespace() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE + ":"))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(URI_WITH_ESCAPED_WHITESPACE));
    }

    @Test
    void handlerThrowsBadRequestWhenQueryParamsIsNotSupported() throws IOException {
        try (var input = requestWithQueryParameters(Map.of(
            INVALID_QUERY_PARAM_KEY, INVALID_QUERY_PARAM_VALUE,
            JsonPropertyNames.QUERY, RANDOM_TITLE))) {

            handler.handleRequest(input, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(body.getDetail(), containsString(
            ErrorMessages.validQueryParameterNamesMessage(VALID_QUERY_PARAMETER_NVA_KEYS)));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsOrganizationUri() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
            JsonPropertyNames.ORGANIZATION, SAMPLE_NVA_ORGANIZATION_ENCODED,
            JsonPropertyNames.LANGUAGE, LANGUAGE_NB))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(SAMPLE_NVA_ORGANIZATION));
    }

    @Test
    void handlerReturnsBadRequestWhenOrganizationUriIsInvalid() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
            JsonPropertyNames.ORGANIZATION, ILLEGAL_NVA_ORGANIZATION_ENCODED,
            JsonPropertyNames.LANGUAGE, LANGUAGE_NB))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerThrowsBadRequestWhenStatusQueryParamsIsInvalid() throws IOException {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
            JsonPropertyNames.STATUS, ILLEGAL_PROJECT_STATUS))) {

            handler.handleRequest(input, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        var value = ErrorMessages.invalidQueryParametersMessageWithRange(
            JsonPropertyNames.STATUS,
            Arrays.toString(ProjectStatus.values())
        );
        assertThat(body.getDetail(),containsString(value));
    }

    @Test
    void shouldAddLangToHttpRequestBeforeSendingToCristin() throws Exception {
        var cristinProjects = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON));
        var oneCristinProject = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON));
        var queryResponse = new HttpResponseFaker(cristinProjects, HTTP_OK);
        var getResponse = new HttpResponseFaker(oneCristinProject, HTTP_OK);
        var mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(queryResponse);
        when(mockHttpClient.<String>sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(getResponse));
        var apiClient = spy(new QueryCristinProjectApiClient(mockHttpClient));
        doReturn(apiClient).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
        sendDefaultQuery();

        var captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockHttpClient).send(captor.capture(), any());

        var expected = "https://api.cristin-test.uio.no/v2/projects?page=1&per_page=5&title=reindeer&lang=en%2Cnb%2Cnn";
        var actual = captor.getValue().uri().toString();

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldAllowLanguageParamForBackwardsCompatibilityEvenIfRedundant() throws Exception {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            JsonPropertyNames.LANGUAGE, LANGUAGE_NB))) {
            handler.handleRequest(input, output, context);
        }
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @ParameterizedTest(
        name = "Handler accepts query parameter status in any case {0}")
    @CsvSource({"ACTIVE", "CONCLUDED", "NOTSTARTED", "active", "concluded", "notstarted", "nOtsTaRtEd"})
    void handlerAcceptsQueryParamsStatusInAnycase(String statusQuery) throws IOException {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
            JsonPropertyNames.ORGANIZATION, SAMPLE_NVA_ORGANIZATION_ENCODED,
            JsonPropertyNames.STATUS, statusQuery))) {

            handler.handleRequest(input, output, context);
        }

        var gatewayResponse =
            GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAddParamsToCristinQueryForFilteringAndReturnOk() throws IOException, ApiGatewayException {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());
        final var queryParams = createQueryParams();
        try (var input = requestWithQueryParameters(queryParams)) {
            handler.handleRequest(input, output, context);
        }
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClientStub).fetchQueryResults(captor.capture());
        var actualURI = captor.getValue().toString();
        assertThat(actualURI, containsString("page=5"));
        assertThat(actualURI, containsString(BIOBANK_ID + EQUAL_OPERATOR + BIOBANK_SAMPLE));
        assertThat(actualURI, containsString(FUNDING + EQUAL_OPERATOR + FUNDING_SAMPLE_ENCODED));
        assertThat(actualURI, containsString(TITLE.getKey() + "=hello"));
        assertThat(actualURI, containsString(PROJECT_KEYWORD + EQUAL_OPERATOR + KEYWORD_SAMPLE));
        assertThat(actualURI, containsString(PROJECT_UNIT + EQUAL_OPERATOR + UNIT_ID_SAMPLE));
        assertThat(actualURI, containsString(PROJECT_SORT + EQUAL_OPERATOR + START_DATE));
        assertThat(actualURI, containsString(PROJECT_CREATOR_PARAM + EQUAL_OPERATOR + CREATOR_IDENTIFIER));
        assertThat(actualURI,
                   containsString(PARTICIPANT_PARAM.getKey() + EQUAL_OPERATOR + CREATOR_IDENTIFIER));
        assertThat(actualURI, containsString(CATEGORY_PARAM + EQUAL_OPERATOR + "PHD"));
        assertThat(actualURI, containsString("sort=start_date%20desc"));

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestToClientWhenUpstreamGetReturnsTheSame() throws ApiGatewayException, IOException {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HTTP_BAD_REQUEST))
            .when(cristinApiClientStub)
            .fetchQueryResults(any());
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
        var gatewayResponse = sendBadParameterRequestQuery();

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(UPSTREAM_RETURNED_BAD_REQUEST));
    }

    @Test
    void shouldReturnOkWithOnlyCreatorPersonIdWhenProjectCreatorHasMissingOrganizationInRoles() throws Exception {
        var serializedCristinProject = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON));
        var deserialized = OBJECT_MAPPER.readValue(serializedCristinProject, CristinProject.class);
        var creatorWithoutRoles = deserialized.getCreator().copy()
                                      .withRoles(null)
                                      .build();
        deserialized.setCreator(creatorWithoutRoles);

        cristinApiClientStub =
            new QueryCristinProjectClientStub(OBJECT_MAPPER.writeValueAsString(deserialized));
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
        var gatewayResponse = sendDefaultQuery();

        List<NvaProject> responseHits =
            OBJECT_MAPPER.convertValue(gatewayResponse.getBodyObject(SearchResponse.class).getHits(),
                                       new TypeReference<>() {});

        var creatorPersonId = responseHits.get(0).getCreator().identity().getId().toString();

        assertThat(creatorPersonId, containsString(CREATOR_IDENTIFIER));
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAddFacetParamsToCristinQuery() throws IOException, ApiGatewayException {
        var apiClient = spy(QueryProjectWithFacetsClient.class);
        var queryResponse = new HttpResponseFaker("{}", 200);
        doReturn(queryResponse).when(apiClient).fetchQueryResults(any());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());
        final var queryParams = Map.of("query", "hello",
                                       "sort", "start_date desc",
                                       "sectorFacet", "UC,INSTITUTE",
                                       "coordinatingFacet", "185.90.0.0",
                                       "responsibleFacet", "20754.0.0.0",
                                       "categoryFacet", "PHD",
                                       "healthProjectFacet", "CLINICAL",
                                       "participantFacet", "5678",
                                       "participantOrgFacet", "194",
                                       "fundingSourceFacet", "REK");
        final var response = sendQueryWithFacets(queryParams, VERSION_2023_11_03_AGGREGATIONS);
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(apiClient).fetchQueryResults(captor.capture());
        var actualURI = captor.getValue().toString();
        assertThat(actualURI, containsString("sort=start_date%20desc"));
        assertThat(actualURI, containsString(SECTOR_FACET.getKey() + EQUAL_OPERATOR + "INSTITUTE"));
        assertThat(actualURI, containsString(SECTOR_FACET.getKey() + EQUAL_OPERATOR + "UC"));
        assertThat(actualURI, containsString(COORDINATING_FACET.getKey() + EQUAL_OPERATOR + "185.90.0.0"));
        assertThat(actualURI, containsString(RESPONSIBLE_FACET.getKey() + EQUAL_OPERATOR + "20754.0.0.0"));
        assertThat(actualURI, containsString(CATEGORY_FACET.getKey() + EQUAL_OPERATOR + "PHD"));
        assertThat(actualURI, containsString(HEALTH_FACET.getKey() + EQUAL_OPERATOR + "CLINICAL"));
        assertThat(actualURI, containsString(PARTICIPANT_FACET.getKey() + EQUAL_OPERATOR + "5678"));
        assertThat(actualURI, containsString(PARTICIPATING_PERSON_ORG_FACET.getKey() + EQUAL_OPERATOR + "194"));
        assertThat(actualURI, containsString(FUNDING_SOURCE_FACET.getKey() + EQUAL_OPERATOR + "REK"));

        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {VERSION_DATE_AGGREGATIONS, VERSION_2023_11_03_AGGREGATIONS, VERSION_NAME_AGGREGATIONS})
    void shouldAddFacetsToSearchResponse(String acceptHeader) throws Exception {
        var apiClient = spy(QueryProjectWithFacetsClient.class);
        var queryResponse = dummyFacetHttpResponse();
        doReturn(queryResponse).when(apiClient).fetchQueryResults(any());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());

        final var queryParams = Map.of("query", "hello",
                                       "sectorFacet", "UC,INSTITUTE",
                                       "coordinatingFacet", "185.90.0.0",
                                       "responsibleFacet", "20754.0.0.0",
                                       "categoryFacet", "PHD",
                                       "healthProjectFacet", "CLINICAL",
                                       "participantFacet", "5678",
                                       "participantOrgFacet", "194",
                                       "fundingSourceFacet", "REK");

        var actual = sendQueryWithFacets(queryParams, acceptHeader).getBodyObject(SearchResponse.class);

        assertThat(actual.getAggregations().size(), equalTo(8));
        assertThat(actual.getHits().size(), equalTo(1));
    }

    @Test
    void shouldReturnErrorMessageWithAllFacetParamsMentioned() throws Exception {
        var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                        .withBody(null)
                        .withQueryParameters(Map.of(INVALID_QUERY_PARAM_KEY, INVALID_QUERY_PARAM_VALUE))
                        .withHeaders(Map.of(ACCEPT, VERSION_NAME_AGGREGATIONS))
                        .build();

        handler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var body = gatewayResponse.getBodyObject(Problem.class);

        Stream.of(CristinFacetParamKey.values())
            .dropWhile(hasUnsupportedFacet())
            .forEach(facetKey -> assertThat(body.getDetail(), containsString(facetKey.getNvaKey())));
    }

    @Test
    void shouldAllowBothFacetAndRegularParamsToCoexistAsOneWhenHavingSameCristinKey()
        throws IOException, ApiGatewayException {

        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());
        final var queryParams = Map.of("query", "hello",
                                       "funding_source", "NFR",
                                       "fundingSourceFacet", "REK",
                                       "participant", "12345",
                                       "participantFacet", "98765",
                                       "category", "PHD",
                                       "categoryFacet", "Master");
        try (var input = requestWithQueryParameters(queryParams)) {
            handler.handleRequest(input, output, context);
        }
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClientStub).fetchQueryResults(captor.capture());
        var actualURI = captor.getValue().toString();
        assertThat(actualURI, containsString("funding_source=NFR"));
        assertThat(actualURI, containsString("funding_source=REK"));
        assertThat(actualURI, containsString("participant=12345"));
        assertThat(actualURI, containsString("participant=98765"));
        assertThat(actualURI, containsString("category=PHD"));
        assertThat(actualURI, containsString("category=Master"));

        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("differentFacetParamsProvider")
    void shouldConvertRegularParamsToFacetParamsWhenTheyMatchAndAreUsingFacetsVersionWhileAlsoAvoidingDoubleEncoding(
        Map<String, String> queryParams
    )
        throws Exception {

        var apiClient = spy(QueryProjectWithFacetsClient.class);
        var queryResponse = dummyFacetHttpResponse();
        var captor = ArgumentCaptor.forClass(URI.class);
        doReturn(queryResponse).when(apiClient).fetchQueryResults(captor.capture());
        var ignoreEnriched = new HttpResponseFaker(EMPTY_STRING, 404);
        doReturn(List.of(ignoreEnriched)).when(apiClient).fetchQueryResultsOneByOne(any());
        doReturn(apiClient).when(clientProvider).getVersionWithFacets();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());

        var actual = sendQueryWithFacets(queryParams, VERSION_NAME_AGGREGATIONS).getBodyObject(SearchResponse.class);

        var actualId = actual.getId();

        assertThat(actualId.toString(), containsString("RESEARCH"));
        assertThat(actualId.toString(), containsString("TEST"));
        assertThat(actualId.toString(), containsString("MORERESEARCH"));
        assertThat(actualId.toString(), containsString("categoryFacet"));
        assertThat(actualId.toString(), not(containsString("category=")));

        assertThat(actualId.toString(), containsString("1234"));
        assertThat(actualId.toString(), containsString("participantFacet"));
        assertThat(actualId.toString(), not(containsString("participant=")));
        assertThat(actualId.toString(), not(containsString(DOUBLE_ENCODED_COMMA_DELIMITER)));

        var categoryFacetString = actual.getAggregations().get("categoryFacet").toString();
        var sectorFacetString = actual.getAggregations().get("sectorFacet").toString();

        assertThat(categoryFacetString, not(containsString(DOUBLE_ENCODED_COMMA_DELIMITER)));
        assertThat(sectorFacetString, not(containsString(DOUBLE_ENCODED_COMMA_DELIMITER)));

        var cristinUri = captor.getValue().toString();

        assertThat(cristinUri, containsString("category=RESEARCH"));
        assertThat(cristinUri, containsString("category=TEST"));
        assertThat(cristinUri, containsString("category=MORERESEARCH"));

        assertThat(cristinUri, containsString("sector=UC"));
        assertThat(cristinUri, containsString("sector=INSTITUTE"));
    }

    @ParameterizedTest(name = "Special character string \"{0}\" should return results")
    @MethodSource("differentQueryParamsProvider")
    void shouldReturnResultsEvenWhenUsingSpecialCharacters(String param, String processedParam) throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());
        final var queryParams = Map.of("query", param);
        final var response = sendQuery(queryParams);
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClientStub).fetchQueryResults(captor.capture());
        var actualURI = captor.getValue().toString();

        assertThat(actualURI, containsString(processedParam));
        assertEquals(HTTP_OK, response.getStatusCode());
    }

    @Test
    void shouldAddParamMultipleToCristinQuery() throws IOException, ApiGatewayException {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, new Environment());
        final var queryParams = Map.of("multiple", "hello");
        try (var input = requestWithQueryParameters(queryParams)) {
            handler.handleRequest(input, output, context);
        }
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClientStub).fetchQueryResults(captor.capture());
        var actualURI = captor.getValue().toString();
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertThat(actualURI, containsString("multiple=hello"));
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    private static Stream<Arguments> differentQueryParamsProvider() {
        return Stream.of(
            Arguments.of("årsstudium",
                         "%C3%A5rsstudium"),
            Arguments.of("Et prestisjefylt, stort og viktig prosjekt",
                         "Et%20prestisjefylt%2C%20stort%20og%20viktig%20prosjekt"),
            Arguments.of("SVIP (styrket veiledning i praksis)",
                         "SVIP%20%28styrket%20veiledning%20i%20praksis%29"),
            Arguments.of("Helsefagarbeiderutdanningen – økt gjennomføring",
                         "Helsefagarbeiderutdanningen%20%E2%80%93%20%C3%B8kt%20gjennomf%C3%B8ring"),
            Arguments.of("<script>alert(1)</script>",
                         "%3Cscript%3Ealert%281%29%3C%2Fscript%3E")
        );
    }

    private static Stream<Arguments> provideDifferentPaginationValuesAndAssertNextAndPreviousResultsIsCorrect() {
        return Stream.of(
            Arguments.of(LINK_EXAMPLE_VALUE,
                         exampleUriFromPageAndResults("201", "1"),
                         exampleUriFromPageAndResults("199", "1"),
                         "200", "1"),
            Arguments.of(LINK_EXAMPLE_VALUE,
                         exampleUriFromPageAndResults("6", "3"),
                         exampleUriFromPageAndResults("4", "3"),
                         "5", "3"),
            Arguments.of(LINK_EXAMPLE_VALUE,
                         EMPTY_STRING,
                         exampleUriFromPageAndResults("24", "10"),
                         "25", "10"),
            Arguments.of(LINK_EXAMPLE_VALUE,
                         exampleUriFromPageAndResults("2", "5"),
                         EMPTY_STRING,
                         "1", "5"),
            Arguments.of(LINK_EXAMPLE_VALUE,
                         exampleUriFromPageAndResults("10", "7"),
                         exampleUriFromPageAndResults("8", "7"),
                         "9", "7"),
            Arguments.of(Constants.REL_NEXT,
                         exampleUriFromPageAndResults("2", "5"),
                         EMPTY_STRING,
                         "1", "5")
        );
    }

    private static String exampleUriFromPageAndResults(String page, String results) {
        var url = "https://api.dev.nva.aws.unit.no/cristin/project?page=%s&title=reindeer&results=%s";
        return String.format(url, page, results);
    }

    private void fakeAnEmptyResponseFromQueryAndEnrichment() throws ApiGatewayException {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HTTP_OK, generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE)))
            .when(cristinApiClientStub).queryProjects(any());
        doReturn(Collections.emptyList())
            .when(cristinApiClientStub).fetchQueryResultsOneByOne(any());
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
    }

    private void modifyQueryResponseToClient(String body, java.net.http.HttpHeaders headers)
        throws ApiGatewayException {

        cristinApiClientStub = spy(cristinApiClientStub);
        var response = new HttpResponseFaker(body);
        response = spy(response);
        doReturn(headers).when(response).headers();
        doReturn(response).when(cristinApiClientStub).fetchQueryResults(any(URI.class));
        doReturn(cristinApiClientStub).when(clientProvider).getVersionOne();
        handler = new QueryCristinProjectHandler(clientProvider, environment);
    }

    private java.net.http.HttpHeaders generateHeaders(String totalCount, String link) {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(totalCount, link), HttpResponseFaker.filter());
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendDefaultQuery() throws IOException {
        try (var input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, RANDOM_TITLE))) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendQuery(Map<String, String> params) throws IOException {
        try (var input = requestWithQueryParameters(params)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendBadParameterRequestQuery() throws IOException {
        try (var input = requestWithQueryParameters(Map.of(
            JsonPropertyNames.QUERY, RANDOM_TITLE,
            PROJECT_SORT, BAD_PARAM_FOR_SORT))) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withQueryParameters(map)
                   .build();
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }

    private HttpResponse<String> dummyFacetHttpResponse() {
        return new HttpResponseFaker(RESPONSE_WITH_FACETS, 200);
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendQueryWithFacets(Map<String, String> facetParams, String acceptValue)
        throws IOException {

        var acceptHeader = Map.of(ACCEPT, acceptValue);

        var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                        .withBody(null)
                        .withQueryParameters(facetParams)
                        .withHeaders(acceptHeader)
                        .build();

        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private static Predicate<CristinFacetParamKey> hasUnsupportedFacet() {
        return facetKey -> facetKey.equals(CristinFacetParamKey.INSTITUTION_PARAM);
    }

    private static ConcurrentHashMap<String, String> createQueryParams() {
        final var queryParams = new ConcurrentHashMap<>(Map.of("query", "hello",
                                                               "funding", FUNDING_SAMPLE,
                                                               "biobank", BIOBANK_SAMPLE,
                                                               "keyword", KEYWORD_SAMPLE,
                                                               "results", "5",
                                                               "unit", UNIT_ID_SAMPLE,
                                                               "sort", START_DATE,
                                                               "creator", CREATOR_IDENTIFIER,
                                                               "participant", CREATOR_IDENTIFIER,
                                                               "category", "PHD"));
        queryParams.put("sort", "start_date desc"); // Map.of() supports only 10 arguments max
        return queryParams;
    }

    private static Stream<Arguments> differentFacetParamsProvider() {
        return Stream.of(
            Arguments.of(Map.of("query", "hello",
                                "categoryFacet", "RESEARCH%2CTEST",
                                "category", "MORERESEARCH",
                                "sector", "UC%2CINSTITUTE",
                                "participant", "1234")),
            Arguments.of(Map.of("query", "hello",
                                "categoryFacet", "RESEARCH,TEST",
                                "category", "MORERESEARCH",
                                "sector", "UC,INSTITUTE",
                                "participant", "1234"))
        );
    }

}
