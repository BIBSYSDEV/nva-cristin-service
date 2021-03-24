package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.ID;
import static no.unit.nva.cristin.projects.RequestUtils.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.projects.RequestUtils.LANGUAGE_QUERY_PARAMETER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Map;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FetchOneCristinProjectTest {

    private static final String EMPTY_JSON = "{}";
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

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.objectMapper;
    private CristinApiClient cristinApiClientStub;
    private Environment environment = new Environment();
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
    void handlerReturnsEmptyJsonWhenIdIsNotFound() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(getReader(CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON))
            .when(cristinApiClientStub).fetchGetResult(any());

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> response = sendQueryWithId(DEFAULT_ID);

        assertEquals(OBJECT_MAPPER.readTree(EMPTY_JSON), OBJECT_MAPPER.readTree(response.getBody()));
    }

    @Test
    void handlerReturnsBadRequestWhenIdIsNotANumber() throws Exception {
        GatewayResponse<NvaProject> response = sendQueryWithId(NOT_AN_ID);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handlerReturnsNvaProjectFromTransformedCristinProjectWhenIdIsFound() throws Exception {
        GatewayResponse<NvaProject> response = sendQueryWithId(DEFAULT_ID);
        var expected = getReader(API_RESPONSE_ONE_PROJECT_JSON);
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(response.getBody()));
    }

    @Test
    void handlerReturnsEmptyJsonWhenFetchFromBackendFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(new IOException()).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> response = sendQueryWithId(DEFAULT_ID);

        assertEquals(OBJECT_MAPPER.readTree(EMPTY_JSON), OBJECT_MAPPER.readTree(response.getBody()));

        doThrow(new FileNotFoundException()).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> nextResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(OBJECT_MAPPER.readTree(EMPTY_JSON), OBJECT_MAPPER.readTree(nextResponse.getBody()));
    }

    @Test
    void handlerReturnsNvaProjectWithoutParticipantsAndCoordinatingInstitutionIfTheyAreMissingFromBackend()
        throws Exception {

        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(getReader(CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON))
            .when(cristinApiClientStub).fetchGetResult(any());

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> response = sendQueryWithId(DEFAULT_ID);

        var expected = getReader(API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON);
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(response.getBody()));
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

    private InputStreamReader getReader(String resource) {
        InputStream queryResultsAsStream = IoUtils.inputStreamFromResources(resource);
        return new InputStreamReader(queryResultsAsStream, Charsets.UTF_8);
    }
}
