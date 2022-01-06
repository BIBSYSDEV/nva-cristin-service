package no.unit.nva.cristin.organization;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.emptyList;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import nva.commons.core.attempt.Try;

class QueryCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    private static final String CRISTIN_QUERY_RESPONSE = "cristin_query_response_sample.json";
    QueryCristinOrganizationHandler queryCristinOrganizationHandler;
    private CristinOrganizationApiClient cristinApiClient;
    private ByteArrayOutputStream output;
    private Context context;

    @BeforeEach
    void setUp() throws NotFoundException, FailedHttpRequestException, InterruptedException {
        context = mock(Context.class);
        cristinApiClient = mock(CristinOrganizationApiClient.class);
        when(cristinApiClient.queryOrganizations(any())).thenReturn(emptySearchResponse());
        output = new ByteArrayOutputStream();
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(cristinApiClient, new Environment());
    }

    private SearchResponse<Organization> emptySearchResponse() {
        return new SearchResponse<Organization>(
                new UriWrapper(HTTPS, DOMAIN_NAME)
                        .addChild(BASE_PATH)
                        .addChild(UriUtils.INSTITUTION)
                        .getUri())
                .withProcessingTime(0L)
                .withSize(0)
                .withHits(emptyList());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingQueryParam() throws IOException {
        InputStream inputStream = generateHandlerRequestWithMissingQueryParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    void shouldReturnEmptyResponseOnStrangeQuery() throws IOException {
        InputStream inputStream = generateHandlerRequestWithStrangeQueryParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse = GatewayResponse.fromOutputStream(output);

        SearchResponse<Organization> actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(0, actual.getHits().size());
        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnResponseOnQuery() throws IOException, ApiGatewayException, InterruptedException {

        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        output = new ByteArrayOutputStream();
        CristinOrganizationApiClient cristinApiClient = new CristinOrganizationApiClient(httpClient);
        CristinOrganizationApiClient mySpy = spy(cristinApiClient);

        final URI first = getCristinUri("185.53.18.14", UNITS_PATH);
        doReturn(getOrganization("org_18_53_18_14.json")).when(mySpy).getOrganization(first);
        final URI second = getCristinUri("2012.9.20.0", UNITS_PATH);
        doReturn(getOrganization("org_2012_9_20_0.json")).when(mySpy).getOrganization(second);
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.statusCode()).thenReturn(HTTP_OK);
        when(mockHttpResponse.body()).thenReturn(IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE)));
        when(mockHttpResponse.headers())
                .thenReturn(java.net.http.HttpHeaders.of(Collections.emptyMap(), (s1,s2) -> true));
        doReturn(getTry(mockHttpResponse)).when(mySpy).sendRequestMultipleTimes(any());


        output = new ByteArrayOutputStream();
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(cristinApiClient, new Environment());

        InputStream inputStream = new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of("query", "Department of Medical Biochemistry"))
                .build();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse = GatewayResponse.fromOutputStream(output);

        SearchResponse<Organization> actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(2, actual.getHits().size());
        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private Try<HttpResponse<String>> getTry(HttpResponse<String> mockHttpResponse) {
        return Try.of(mockHttpResponse);
    }

    private Organization getOrganization(String subUnitFile) throws JsonProcessingException {
        return  OBJECT_MAPPER.readValue(IoUtils.stringFromResources(Path.of(subUnitFile)), Organization.class);
    }

    private InputStream generateHandlerRequestWithMissingQueryParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .build();
    }

    private InputStream generateHandlerRequestWithStrangeQueryParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of("query", "strangeQueryWithoutHits"))
                .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

}
