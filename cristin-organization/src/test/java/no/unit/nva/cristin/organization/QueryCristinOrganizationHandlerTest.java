package no.unit.nva.cristin.organization;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.MediaTypes;
import nva.commons.core.JsonUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    QueryCristinOrganizationHandler queryCristinOrganizationHandler;
    private CristinApiClient cristinApiClient;
    private ByteArrayOutputStream output;
    private Context context;


    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        cristinApiClient = mock(CristinApiClient.class);
        when(cristinApiClient.queryInstitutions(any())).thenReturn(emptySearchResponse());
        output = new ByteArrayOutputStream();
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(cristinApiClient);
    }

    private SearchResponse<Organization> emptySearchResponse() {
        return new SearchResponse<Organization>(
                new UriWrapper(HTTPS, DOMAIN_NAME)
                        .addChild(BASE_PATH)
                        .addChild(UriUtils.INSTITUTION)
                        .getUri())
                .withProcessingTime(0L)
                .withSize(0)
                .withHits(Collections.emptyList());
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

//    @Test
    void shouldReturnResponseOnQuery() throws IOException { // TODO Integration test...

        cristinApiClient = new CristinApiClient();
        output = new ByteArrayOutputStream();
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(cristinApiClient);

        InputStream inputStream = generateHandlerRequestWithStrangeQueryParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse = GatewayResponse.fromOutputStream(output);

        SearchResponse<Organization> actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(0, actual.getHits().size());
        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
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

    private URI randomId() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(ORGANIZATION_PATH)
                .addChild(randomString()).getUri();
    }
}
