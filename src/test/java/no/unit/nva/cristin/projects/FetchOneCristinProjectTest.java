package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.ID;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.CristinHandler.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.projects.CristinHandler.LANGUAGE_QUERY_PARAMETER;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static nva.commons.apigateway.ApiGatewayHandler.APPLICATION_PROBLEM_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FetchOneCristinProjectTest {

    private static final String CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON =
        "cristinGetProjectIdNotFoundResponse.json";
    private static final String API_RESPONSE_ONE_PROJECT_JSON =
        "api_response_one_cristin_project_to_nva_project.json";
    private static final String CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON =
        "cristinProjectWithoutInstitutionAndParticipants.json";
    private static final String API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON =
        "api_response_get_project_with_missing_fields.json";
    private static final String NOT_AN_ID = "Not an ID";
    private static final String DEFAULT_ID = "9999";
    private static final String JSON_WITH_MISSING_REQUIRED_DATA = "{\"cristin_project_id\": \"456789\"}";
    private static final String ENGLISH_LANGUAGE = "en";
    private static final String GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI = "https://api.cristin.no/v2/projects/9999?lang=en";

    private CristinApiClient cristinApiClientStub;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchOneCristinProject handler;

    @BeforeEach
    void setUp() {
        cristinApiClientStub = new CristinApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
    }

    @Test
    void handlerReturnsNotFoundStatusWhenIdIsNotFound() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseStub(getStream(CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON), 404))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
    }

    @Test
    void handlerReturnsBadGatewayWhenStatusCodeFromBackendSignalsError() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseStub(null, 500))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsBadRequestWhenIdIsNotANumber() throws Exception {
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(NOT_AN_ID);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID));
    }

    @Test
    void handlerReturnsNvaProjectFromTransformedCristinProjectWhenIdIsFound() throws Exception {
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);
        InputStream expected = getStream(API_RESPONSE_ONE_PROJECT_JSON);
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(gatewayResponse.getBody()));
    }

    @Test
    void handlerReturnsBadGatewayWhenBackendThrowsException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED)).when(cristinApiClientStub)
            .getProject(any(), any());
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerReturnsNvaProjectWithoutParticipantsAndCoordinatingInstitutionIfTheyAreMissingFromBackend()
        throws Exception {

        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(getStream(CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON)))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        InputStream expected = getStream(API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON);
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(gatewayResponse.getBody()));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenBackendReturnsInvalidProjectData() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseStub(IoUtils.stringToStream(JSON_WITH_MISSING_REQUIRED_DATA)))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertTrue(gatewayResponse.getBody().contains(
            String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, DEFAULT_ID)));
    }

    @Test
    void getsCorrectUriWhenCallingGetProjectUriBuilder() throws Exception {
        assertEquals(new URI(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI),
            cristinApiClientStub.generateGetProjectUri(DEFAULT_ID, ENGLISH_LANGUAGE));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenThereIsThrownExceptionWhenReadingFromJson() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(IoUtils.stringToStream("")))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsInternalErrorWhenUriCreationFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(URISyntaxException.class).when(cristinApiClientStub).generateGetProjectUri(any(), any());

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerThrowsInternalErrorWhenHttpStatusCodeIsSomeUnexpectedValue() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(IoUtils.stringToStream(""), 418))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(gatewayResponse.getBody().contains(ERROR_MESSAGE_SERVER_ERROR));
    }

    private GatewayResponse<NvaProject> sendQueryWithId(String id) throws IOException {
        InputStream input = requestWithLanguageAndId(
            Map.of(LANGUAGE_QUERY_PARAMETER, DEFAULT_LANGUAGE_CODE),
            Map.of(ID, id));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithLanguageAndId(Map<String, String> languageQueryParam,
                                                 Map<String, String> idPathParam)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(languageQueryParam)
            .withPathParameters(idPathParam)
            .build();
    }

    private InputStream getStream(String resource) {
        return IoUtils.inputStreamFromResources(resource);
    }
}
