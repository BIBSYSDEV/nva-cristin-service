package no.unit.nva.cristin.projects.update;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.UNSUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomContributors;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomLanguage;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomListOfTitles;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomOrganization;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomStatus;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.language.LanguageMapper.toUri;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Map;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.language.LanguageMapper;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateCristinProjectHandlerTest {

    private static final String PROJECT_IDENTIFIER = "identifier";
    private static final Map<String, String> validPath = Map.of(PROJECT_IDENTIFIER, randomIntegerAsString());

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateCristinProjectHandler handler;
    private final Environment environment = new Environment();

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        UpdateCristinProjectApiClient updateCristinProjectApiClient = new UpdateCristinProjectApiClient(httpClientMock);
        handler = new UpdateCristinProjectHandler(updateCristinProjectApiClient, environment);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        var gatewayResponse = queryWithoutRequiredAccessRights();
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws IOException {
        var gatewayResponse = sendQuery(null);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnNoContentResponseWhenCallingHandlerWithValidJson() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(CONTRIBUTORS, OBJECT_MAPPER.writeValueAsString(randomContributors()));
        jsonObject.put(COORDINATING_INSTITUTION, OBJECT_MAPPER.writeValueAsString(randomOrganization()));
        jsonObject.put(END_DATE, randomInstantString());
        jsonObject.put(LANGUAGE, LanguageMapper.getLanguageByIso6391Code(randomLanguage()).getLexvoUri().toString());
        jsonObject.put(START_DATE, randomInstantString());
        jsonObject.put(TITLE, randomString());
        GatewayResponse<Void> gatewayResponse = sendQuery(jsonObject.toString());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenNoSupportedFieldsArePresent() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ACADEMIC_SUMMARY, getSummaryAsString());
        jsonObject.put(ALTERNATIVE_TITLES, getTitleAsString());
        jsonObject.put(POPULAR_SCIENTIFIC_SUMMARY, getSummaryAsString());
        jsonObject.put(STATUS, OBJECT_MAPPER.writeValueAsString(randomStatus()));
        GatewayResponse<Void> gatewayResponse = sendQuery(jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());

        var keys = new ArrayList<>();
        jsonObject.fieldNames().forEachRemaining(keys::add);
        assertThat(gatewayResponse.getBody(), containsString(String.format(UNSUPPORTED_FIELDS_IN_PAYLOAD, keys)));
    }

    @Test
    void shouldReturnBadRequestWhenTitleHasNoLanguageFieldPresent() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(TITLE, randomString());
        GatewayResponse<Void> gatewayResponse = sendQuery(jsonObject.toString());
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnOKNoContentWhenRequestHasTitleAndLanguagePresent() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(TITLE, randomString());
        jsonObject.put(LANGUAGE, toUri(randomLanguage()).toString());
        GatewayResponse<Void> gatewayResponse = sendQuery(jsonObject.toString());
        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    private String getTitleAsString() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(randomListOfTitles(URI.create(randomLanguage())));
    }

    private String getSummaryAsString() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(randomListOfTitles(URI.create(randomLanguage())).get(0));
    }

    private String randomInstantString() {
        return new DateTimeFormatterBuilder().appendInstant(3).toFormatter().format(randomInstant());
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Void> sendQuery(String body) throws IOException {
        InputStream input = createRequest(body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private InputStream createRequest(String body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, EDIT_OWN_INSTITUTION_PROJECTS)
                   .withPathParameters(validPath)
                   .build();
    }

    private GatewayResponse<Void> queryWithoutRequiredAccessRights() throws IOException {
        InputStream input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(EMPTY_JSON)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Void.class);
    }
}
