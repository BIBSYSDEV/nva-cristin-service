package no.unit.nva.cristin.projects.update;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNamesMap;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.FUNDING_MISSING_REQUIRED_FIELDS;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.MUST_BE_A_LIST;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.TITLE_MUST_HAVE_A_LANGUAGE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.UNSUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomStatus;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import static no.unit.nva.utils.PatchValidator.COULD_NOT_PARSE_LANGUAGE_FIELD;
import static no.unit.nva.utils.PatchValidator.ILLEGAL_VALUE_FOR_PROPERTY;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class UpdateCristinProjectHandlerTest {

    private static final String PROJECT_IDENTIFIER = "identifier";
    private static final Map<String, String> validPath = Map.of(PROJECT_IDENTIFIER, randomIntegerAsString());
    public static final String PATCH_REQUEST_JSON = "nvaApiPatchRequest.json";
    public static final String CRISTIN_PATCH_REQUEST_JSON = "cristinPatchRequest.json";

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
        var input = IoUtils.stringFromResources(Path.of(PATCH_REQUEST_JSON));
        var gatewayResponse = sendQuery(input);

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenNoSupportedFieldsArePresent() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ACADEMIC_SUMMARY, randomNamesMap().toString());
        jsonObject.put(ALTERNATIVE_TITLES, randomNamesMap().toString());
        jsonObject.put(POPULAR_SCIENTIFIC_SUMMARY, randomNamesMap().toString());
        jsonObject.put(STATUS, randomStatus().toString());
        GatewayResponse<Void> gatewayResponse = sendQuery(jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());

        var keys = new ArrayList<>();
        jsonObject.fieldNames().forEachRemaining(keys::add);
        assertThat(gatewayResponse.getBody(), containsString(format(UNSUPPORTED_FIELDS_IN_PAYLOAD, keys)));
    }

    @ParameterizedTest(name = "Exception for field {0} with message {2}")
    @MethodSource("badRequestProvider")
    void shouldReturnBadRequestOnInvalidJson(String field, JsonNode input, String exceptionMessage) throws IOException {
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertThat(gatewayResponse.getBody(), containsString(exceptionMessage));
    }

    @Test
    void shouldProduceCorrectPayloadToSendToCristin() throws Exception {
        var mockHttpClient = mock(HttpClient.class);
        var apiClient = new UpdateCristinProjectApiClient(mockHttpClient);
        apiClient = spy(apiClient);
        handler = new UpdateCristinProjectHandler(apiClient, environment);
        mockUpstream(mockHttpClient);
        var input = IoUtils.stringFromResources(Path.of(PATCH_REQUEST_JSON));
        sendQuery(input);

        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).patch(any(), captor.capture());
        var capturedCristinProjectString = captor.getValue();

        var expectedPayload = IoUtils.stringFromResources(Path.of(CRISTIN_PATCH_REQUEST_JSON));

        assertThat(OBJECT_MAPPER.readTree(capturedCristinProjectString),
                   equalTo(OBJECT_MAPPER.readTree(expectedPayload)));
    }

    private void mockUpstream(HttpClient mockHttpClient) throws IOException, InterruptedException {
        var httpResponse = new HttpResponseFaker("", 204);
        when(mockHttpClient.send(any(), any())).thenAnswer(response -> httpResponse);
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

    private static Stream<Arguments> badRequestProvider() {
        return Stream.of(
            Arguments.of(FUNDING, fundingNotAnArray(), format(MUST_BE_A_LIST, FUNDING)),
            Arguments.of(FUNDING, fundingWithoutRequiredFields(), FUNDING_MISSING_REQUIRED_FIELDS),
            Arguments.of(LANGUAGE, languageNotSupported(), format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE)),
            Arguments.of(LANGUAGE, titlePresentButNotLanguage(), TITLE_MUST_HAVE_A_LANGUAGE),
            Arguments.of(LANGUAGE, languageNotAValidValue(), COULD_NOT_PARSE_LANGUAGE_FIELD)
        );
    }

    private static JsonNode fundingNotAnArray() {
        return OBJECT_MAPPER.createObjectNode().put(FUNDING, randomString());
    }

    private static JsonNode fundingWithoutRequiredFields() {
        var invalidFunding = OBJECT_MAPPER.createObjectNode();
        var invalidSource = OBJECT_MAPPER.createObjectNode().put(randomString(), randomString());
        invalidFunding.set(SOURCE, invalidSource);
        var arrayNode = OBJECT_MAPPER.createArrayNode();
        arrayNode.add(invalidFunding);
        var input = OBJECT_MAPPER.createObjectNode();
        input.set(FUNDING, arrayNode);

        return input;
    }

    private static JsonNode languageNotAValidValue() {
        return OBJECT_MAPPER.createObjectNode().putPOJO(LANGUAGE, List.of(randomInteger(), randomInteger()));
    }

    private static JsonNode languageNotSupported() {
        return OBJECT_MAPPER.createObjectNode().put(LANGUAGE, randomString());
    }

    private static JsonNode titlePresentButNotLanguage() {
        return OBJECT_MAPPER.createObjectNode().put(TITLE, randomString());
    }

}
