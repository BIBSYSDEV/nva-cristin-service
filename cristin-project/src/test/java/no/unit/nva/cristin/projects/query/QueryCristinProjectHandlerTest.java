package no.unit.nva.cristin.projects.query;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.model.JsonPropertyNames;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
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
import org.mockito.ArgumentCaptor;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_TITLE;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


class QueryCristinProjectHandlerTest {

    public static final String LANGUAGE_NB = "nb";
    public static final String RANDOM_TITLE = "reindeer";
    public static final String ZERO_VALUE = "0";
    public static final String TOTAL_COUNT_EXAMPLE_250 = "250";
    public static final String PAGE_15 = "15";
    public static final String GRANT_ID_EXAMPLE = "1234567";
    public static final String WHITESPACE = " ";
    public static final String URI_WITH_ESCAPED_WHITESPACE =
            "https://api.dev.nva.aws.unit.no/cristin/project?query=reindeer+reindeer&language=nb&page=1&results=5";
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
            "https://api.dev.nva.aws.unit.no/cristin/project?query=reindeer&language=nb&page=2&results=5";
    private static final String URI_WITH_TEN_NUMBER_OF_RESULTS =
            "https://api.dev.nva.aws.unit.no/cristin/project?query=reindeer&language=nb&page=1&results=10";
    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String API_RESPONSE_NON_ENRICHED_PROJECTS_JSON = "api_response_non_enriched_projects.json";
    private static final String API_QUERY_RESPONSE_NO_PROJECTS_FOUND_JSON = "api_query_response_no_projects_found.json";
    private static final String SAMPLE_NVA_ORGANIZATION =
            "https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0";
    private static final String SAMPLE_NVA_ORGANIZATION_ENCODED =
            URLEncoder.encode(SAMPLE_NVA_ORGANIZATION, StandardCharsets.UTF_8);
    private static final String ILLEGAL_NVA_ORGANIZATION =
            "hps:/api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0";
    private static final String ILLEGAL_NVA_ORGANIZATION_ENCODED =
            URLEncoder.encode(ILLEGAL_NVA_ORGANIZATION, StandardCharsets.UTF_8);
    private static final String API_QUERY_RESPONSE_WITH_FUNDING_JSON =
        IoUtils.stringFromResources(Path.of("api_query_response_with_funding.json"));
    public static final String FUNDING_SAMPLE = "NRE:1234";
    public static final String BIOBANK_SAMPLE = String.valueOf(randomInteger());
    public static final String KEYWORD_SAMPLE = randomString();
    public static final String UNIT_ID_SAMPLE = "184.12.60.0";
    public static final String START_DATE = "start_date";
    public static final String NB = "nb";

    private final Environment environment = new Environment();
    private QueryCristinProjectApiClient cristinApiClientStub;
    private Context context;
    private ByteArrayOutputStream output;
    private QueryCristinProjectHandler handler;

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
        String url = "https://api.dev.nva.aws.unit.no/cristin/project?query=reindeer&language=nb&page=%s&results=%s";
        return String.format(url, page, results);
    }

    @BeforeEach
    void setUp() {
        cristinApiClientStub = new QueryCristinProjectClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);
    }

    @ParameterizedTest
    @MethodSource("queryResponseWithFundingFileReader")
    void handlerReturnsExpectedBodyWhenRequestInputIsValid(String expected) throws IOException {
        String actual = sendDefaultQuery().getBody();
        final var expectedSearchResponse = Constants.OBJECT_MAPPER.readValue(expected, SearchResponse.class);
        final var actualSearchResponse = Constants.OBJECT_MAPPER.readValue(actual, SearchResponse.class);
        assertEquals(expectedSearchResponse, actualSearchResponse);
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
                .getEnrichedProjectsUsingQueryResponse(any(), any());
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);

        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerReturnsNonEnrichedBodyWhenEnrichingFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        HttpResponse<String> response =
                new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(CompletableFuture.completedFuture(response))
                .when(cristinApiClientStub).fetchGetResultAsync(any());
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);
        var gatewayResponse = sendDefaultQuery();
        String expected = getBodyFromResource(API_RESPONSE_NON_ENRICHED_PROJECTS_JSON);

        assertEquals(Constants.OBJECT_MAPPER.readTree(expected),
                Constants.OBJECT_MAPPER.readTree(gatewayResponse.getBody()));
    }

    @Test
    void handlerThrowsBadRequestWhenMissingTitleQueryParameter() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.LANGUAGE, LANGUAGE_NB));

        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                containsString(ErrorMessages.invalidQueryParametersMessage(JsonPropertyNames.QUERY,
                        ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE)));
    }

    @Test
    void handlerSetsDefaultValueForMissingOptionalLanguageParameterAndReturnOk() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, RANDOM_TITLE));

        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,Object.class);

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
        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, EMPTY_STRING));

        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                containsString(ErrorMessages.invalidQueryParametersMessage(JsonPropertyNames.QUERY,
                        ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE)));
    }

    @Test
    void handlerReturnsBadRequestWhenReceivingTitleQueryParamWithIllegalCharacters() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, TITLE_ILLEGAL_CHARACTERS));

        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                containsString(ErrorMessages.invalidQueryParametersMessage(JsonPropertyNames.QUERY,
                        ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE)));
    }

    @Test
    void handlerReturnsBadRequestWhenReceivingInvalidLanguageQueryParam() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, INVALID_LANGUAGE));

        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                containsString(String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE, JsonPropertyNames.LANGUAGE)));
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
        String expected = getBodyFromResource(API_QUERY_RESPONSE_NO_PROJECTS_FOUND_JSON);
        var gatewayResponse = sendDefaultQuery();

        assertEquals(Constants.OBJECT_MAPPER.readTree(expected),
                Constants.OBJECT_MAPPER.readTree(gatewayResponse.getBody()));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class).when(cristinApiClientStub)
                .generateQueryProjectsUrl(any(), any(Constants.QueryType.class));
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);
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
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);
        var gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsPageParameter() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, SECOND_PAGE));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(URI_WITH_PAGE_NUMBER_VALUE_OF_TWO));
    }

    @Test
    void handlerThrowsBadRequestWhenQueryHasInvalidPageParameter() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, TITLE_ILLEGAL_CHARACTERS));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

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

        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, currentPage,
                JsonPropertyNames.NUMBER_OF_RESULTS, perPage
        ));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);
        Integer actual = Constants.OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SearchResponse.class)
                .getFirstRecord();
        assertEquals(expected, actual);
    }

    @Test
    void handlerThrowsBadRequestWhenPaginationExceedsNumberOfResults() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, PAGE_15,
                JsonPropertyNames.NUMBER_OF_RESULTS, TEN_RESULTS
        ));
        handler.handleRequest(input, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);

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

        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, SECOND_PAGE,
                JsonPropertyNames.NUMBER_OF_RESULTS, TEN_RESULTS
        ));
        handler.handleRequest(input, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(),
                containsString(String.format(ErrorMessages.ERROR_MESSAGE_PAGE_OUT_OF_SCOPE, ZERO_VALUE)));
    }

    @ParameterizedTest(
            name = "Handler throws bad request when supplying non positive integer for results {0} or page {1}")
    @CsvSource({"0,5", "5,0"})
    void handlerThrowsBadRequestWhenSuppliedWithNonPositiveIntegerForPageOrResults(String perPage, String currentPage)
            throws Exception {

        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, currentPage,
                JsonPropertyNames.NUMBER_OF_RESULTS, perPage
        ));
        handler.handleRequest(input, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(), anyOf(
                containsString(String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE,
                        JsonPropertyNames.NUMBER_OF_RESULTS)),
                containsString(String.format(ErrorMessages.ERROR_MESSAGE_INVALID_VALUE, JsonPropertyNames.PAGE))));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsResultsParameter() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.NUMBER_OF_RESULTS, TEN_RESULTS));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(URI_WITH_TEN_NUMBER_OF_RESULTS));
    }

    @Test
    void handlerThrowsBadRequestWhenQueryHasInvalidResultsParameter() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.NUMBER_OF_RESULTS, TITLE_ILLEGAL_CHARACTERS));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

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

        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB,
                JsonPropertyNames.PAGE, currentPage,
                JsonPropertyNames.NUMBER_OF_RESULTS, perPage
        ));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        String actualNext =
                Optional.ofNullable(Constants.OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SearchResponse.class)
                        .getNextResults()).orElse(new URI(EMPTY_STRING)).toString();
        assertEquals(expectedNext, actualNext);

        String actualPrevious =
                Optional.ofNullable(Constants.OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SearchResponse.class)
                        .getPreviousResults()).orElse(new URI(EMPTY_STRING)).toString();
        assertEquals(expectedPrevious, actualPrevious);
    }

    @Test
    void handlerReturnsMatchingProjectsFromGrantIdSearchWhenSuppliedWithOnlyNumber() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(
                getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE)))
                .when(cristinApiClientStub).queryProjects(any(), eq(QUERY_USING_GRANT_ID));

        doThrow(RuntimeException.class)
                .when(cristinApiClientStub).queryProjects(any(), eq(QUERY_USING_TITLE));

        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);

        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, GRANT_ID_EXAMPLE));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsProjectsFromTitleSearchWhenSuppliedWithQueryStringIncludingNumber() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class)
                .when(cristinApiClientStub).queryProjects(any(), eq(QUERY_USING_GRANT_ID));

        doReturn(new HttpResponseFaker(
                getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE)))
                .when(cristinApiClientStub).queryProjects(any(), eq(QUERY_USING_TITLE));

        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);

        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY,
                GRANT_ID_EXAMPLE + RANDOM_TITLE));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsProjectsFromTitleSearchWhenGrantIdSearchReturnsZeroResults() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING))
                .when(cristinApiClientStub).queryProjects(any(), eq(QUERY_USING_GRANT_ID));

        doReturn(new HttpResponseFaker(
                getBodyFromResource(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE)))
                .when(cristinApiClientStub).queryProjects(any(), eq(QUERY_USING_TITLE));

        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);

        InputStream input = requestWithQueryParameters(Map.of(JsonPropertyNames.QUERY, GRANT_ID_EXAMPLE));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        var actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(5, actual.getHits().size());
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MEDIATYPE_JSON_UTF8, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsTitleWithWhitespace() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(URI_WITH_ESCAPED_WHITESPACE));
    }

    @Test
    void handlerThrowsBadRequestWhenQueryParamsIsNotSupported() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(
                INVALID_QUERY_PARAM_KEY, INVALID_QUERY_PARAM_VALUE,
                JsonPropertyNames.QUERY, RANDOM_TITLE));

        handler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        Problem body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(body.getDetail(), containsString(
                ErrorMessages.validQueryParameterNamesMessage(QueryCristinProjectHandler.VALID_QUERY_PARAMETERS)));
    }

    @Test
    void handlerReturnsCristinProjectsWhenQueryContainsOrganizationUri() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
                JsonPropertyNames.ORGANIZATION, SAMPLE_NVA_ORGANIZATION_ENCODED,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(SAMPLE_NVA_ORGANIZATION_ENCODED));
    }

    @Test
    void handlerReturnsBadRequestWhenOrganizationUriIsInvalid() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
                JsonPropertyNames.ORGANIZATION, ILLEGAL_NVA_ORGANIZATION_ENCODED,
                JsonPropertyNames.LANGUAGE, LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerThrowsBadRequestWhenStatusQueryParamsIsInvalid() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
                JsonPropertyNames.STATUS, ILLEGAL_PROJECT_STATUS));

        handler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        Problem body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(body.getDetail(),
                containsString(
                        ErrorMessages.invalidQueryParametersMessageWithRange(JsonPropertyNames.STATUS,
                                Arrays.toString(ProjectStatus.values()))));
    }

    @ParameterizedTest(
            name = "Handler accepts query parameter status in any case {0}")
    @CsvSource({"ACTIVE", "CONCLUDED", "NOTSTARTED", "active", "concluded", "notstarted", "nOtsTaRtEd"})
    void handlerAcceptsQueryParamsStatusInAnycase(String statusQuery) throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY, RANDOM_TITLE + WHITESPACE + RANDOM_TITLE,
                JsonPropertyNames.ORGANIZATION, SAMPLE_NVA_ORGANIZATION_ENCODED,
                JsonPropertyNames.STATUS, statusQuery));

        handler.handleRequest(input, output, context);

        var gatewayResponse =
                GatewayResponse.fromOutputStream(output, SearchResponse.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    private void fakeAnEmptyResponseFromQueryAndEnrichment() throws ApiGatewayException {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HTTP_OK,
                generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE)))
                .when(cristinApiClientStub).queryProjects(any(), any());
        doReturn(Collections.emptyList()).when(cristinApiClientStub).fetchQueryResultsOneByOne(any());
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);
    }

    private void modifyQueryResponseToClient(String body, java.net.http.HttpHeaders headers)
            throws ApiGatewayException {

        cristinApiClientStub = spy(cristinApiClientStub);
        HttpResponse<String> response = new HttpResponseFaker(body);
        response = spy(response);
        doReturn(headers).when(response).headers();
        doReturn(response).when(cristinApiClientStub).fetchQueryResults(any(URI.class));
        handler = new QueryCristinProjectHandler(cristinApiClientStub, environment);
    }

    private java.net.http.HttpHeaders generateHeaders(String totalCount, String link) {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(totalCount, link), HttpResponseFaker.filter());
    }

    private GatewayResponse<SearchResponse> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(
                JsonPropertyNames.QUERY,
                RANDOM_TITLE,
                JsonPropertyNames.LANGUAGE,
                LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(Constants.OBJECT_MAPPER)
                .withBody(null)
                .withQueryParameters(map)
                .build();
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }

    private static Stream<? extends Arguments> queryResponseWithFundingFileReader() {
        return Stream.of(Arguments.of(API_QUERY_RESPONSE_WITH_FUNDING_JSON));
    }


    @Test
    void shouldAddParamsToCristinQueryForFilteringAndReturnOk() throws IOException, ApiGatewayException {
        cristinApiClientStub = spy(cristinApiClientStub);
        handler = new QueryCristinProjectHandler(cristinApiClientStub, new Environment());
        var queryParams = Map.of("query", "hello",
                "funding", FUNDING_SAMPLE,
                "biobank", BIOBANK_SAMPLE,
                "keyword", KEYWORD_SAMPLE,
                "results", "5",
                "unit", UNIT_ID_SAMPLE,
                "sort", START_DATE);
        var input = requestWithQueryParameters(queryParams);
        handler.handleRequest(input, output, context);
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClientStub).fetchQueryResults(captor.capture());
        var actualURI = captor.getValue().toString();
        assertThat(actualURI,
                containsString("page=5"));
        assertThat(actualURI,
                containsString("&biobank="+BIOBANK_SAMPLE));
        assertThat(actualURI,
                containsString("&funding="+FUNDING_SAMPLE));
        assertThat(actualURI,
                containsString("&lang="+ NB));
        assertThat(actualURI,
                containsString("&title=hello"));
        assertThat(actualURI,
                containsString("&keyword="+KEYWORD_SAMPLE));
        assertThat(actualURI,
                containsString("&unit="+UNIT_ID_SAMPLE));
        assertThat(actualURI,
                containsString("&sort="+START_DATE));

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }
}
