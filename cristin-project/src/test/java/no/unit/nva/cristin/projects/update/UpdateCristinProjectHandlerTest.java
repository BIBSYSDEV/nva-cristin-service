package no.unit.nva.cristin.projects.update;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTACT_INFO;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_CONTACT_INFO;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_CATEGORIES;
import static no.unit.nva.cristin.model.JsonPropertyNames.RELATED_PROJECTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.common.ProjectHandlerAccessCheck.MANAGE_OWN_PROJECTS;
import static no.unit.nva.cristin.projects.model.cristin.CristinContactInfo.CRISTIN_CONTACT_PERSON;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.EQUIPMENT;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.METHOD;
import static no.unit.nva.cristin.projects.model.nva.ContactInfo.CONTACT_PERSON;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.KEYWORDS;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.FUNDING_MISSING_REQUIRED_FIELDS_OR_NOT_VALID;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.KEYWORDS_MISSING_REQUIRED_FIELD_TYPE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.MUST_BE_A_LIST;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.MUST_BE_A_LIST_OF_IDENTIFIERS;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.NOT_A_VALID_KEY_VALUE_FIELD;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.NOT_A_VALID_LIST_OF_KEY_VALUE_FIELDS;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.NOT_A_VALID_URI;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.TITLE_MUST_HAVE_A_LANGUAGE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.WEB_PAGE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.PROJECT_CATEGORIES_MISSING_REQUIRED_FIELD_TYPE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.validation.PatchValidator.COULD_NOT_PARSE_LANGUAGE_FIELD;
import static no.unit.nva.validation.PatchValidator.ILLEGAL_VALUE_FOR_PROPERTY;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.projects.fetch.FetchCristinProjectApiClient;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.exception.GatewayTimeoutException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

class UpdateCristinProjectHandlerTest {

    private static final String PROJECT_IDENTIFIER = "identifier";
    private static final Map<String, String> validPath = Map.of(PROJECT_IDENTIFIER, randomIntegerAsString());
    public static final String PATCH_REQUEST_JSON = "nvaApiPatchRequest.json";
    public static final String CRISTIN_PATCH_REQUEST_JSON = "cristinPatchRequest.json";
    public static final String UNSUPPORTED_FIELD = "unsupportedField";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String LANGUAGE_NORWEGIAN = "nb";
    public static final String USER_IDENTIFIER = "12345";
    public static final String CRISTIN_PRO_PARTICIPANT_CODE = "PRO_PARTICIPANT";
    public static final String CRISTIN_PRO_MANAGER_CODE = "PRO_MANAGER";
    public static final String ANOTHER_IDENTIFIER_THAN_USER = "999888";
    public static final String NO_ACCESS = "noAccess";
    public static final String NOT_SOME_WEBPAGE = "<script>1</script>";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final HttpClient httpClientMockFetch = mock(HttpClient.class);
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateCristinProjectHandler handler;
    private FetchCristinProjectApiClient fetchApiClient;
    private final Environment environment = new Environment();
    private UpdateCristinProjectApiClient updateApiClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        var cristinProject = basicCristinProject();
        when(httpClientMockFetch.<String>send(any(), any()))
            .thenReturn(new HttpResponseFaker(cristinProject.toString(), 200));
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        updateApiClient = new UpdateCristinProjectApiClient(httpClientMock);
        updateApiClient = spy(updateApiClient);
        fetchApiClient = new FetchCristinProjectApiClient(httpClientMockFetch);
        fetchApiClient = spy(fetchApiClient);
        handler = new UpdateCristinProjectHandler(updateApiClient, fetchApiClient, environment);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        var gatewayResponse = queryWithoutRequiredAccessRights();
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var gatewayResponse = sendQuery(null);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnNoContentResponseWhenCallingHandlerWithValidJson() throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var input = IoUtils.stringFromResources(Path.of(PATCH_REQUEST_JSON));
        var gatewayResponse = sendQuery(input);

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @ParameterizedTest(name = "Exception for field {0} with message {2}")
    @MethodSource("badRequestProvider")
    void shouldReturnBadRequestOnInvalidJson(String field, JsonNode input, String exceptionMessage) throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var gatewayResponse = sendQuery(input.toString());

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(gatewayResponse.getBody(), containsString(exceptionMessage));
    }

    @Test
    void shouldProduceCorrectPayloadToSendToCristin() throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var input = IoUtils.stringFromResources(Path.of(PATCH_REQUEST_JSON));
        sendQuery(input);

        var actualPayload = captureCristinPayload(updateApiClient);
        var expectedPayload = IoUtils.stringFromResources(Path.of(CRISTIN_PATCH_REQUEST_JSON));

        assertThat(OBJECT_MAPPER.readTree(actualPayload), equalTo(OBJECT_MAPPER.readTree(expectedPayload)));
    }

    @ParameterizedTest(name = "Allowing null value for field {0}")
    @ValueSource(strings = {"funding", "relatedProjects", "institutionsResponsibleForResearch", "webPage"})
    void shouldAllowFieldsWhichCanBeNullable(String fieldName) throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var input = OBJECT_MAPPER.createObjectNode().putNull(fieldName);
        var gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldSilentlyIgnoreUnsupportedFieldsInPayload() throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var input = OBJECT_MAPPER.createObjectNode();
        input.put(START_DATE, randomInstant().toString());
        input.put(UNSUPPORTED_FIELD, randomString());
        var gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldDefaultToEmptyStringAndNotCrashWhenSendingNonTextualValuesForContactInfoFields() throws Exception {
        handler = spy(handler);
        doReturn(true).when(handler).hasResourceAccess(any());

        var input = OBJECT_MAPPER.createObjectNode();
        var contactInfo = OBJECT_MAPPER.createObjectNode();
        contactInfo.putPOJO(CONTACT_PERSON, Map.of(randomString(), randomString()));
        input.set(CONTACT_INFO, contactInfo);

        var gatewayResponse = sendQuery(input.toString());
        var actualPayload = captureCristinPayload(updateApiClient);

        var actualContactInfo = OBJECT_MAPPER.readTree(actualPayload).get(CRISTIN_CONTACT_INFO);

        assertThat(actualContactInfo.get(CRISTIN_CONTACT_PERSON).asText(), equalTo(EMPTY_STRING));
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnForbiddenOnUpdateOfProjectWhenHavingLegacyAccessRight() throws IOException {
        final var legacyAccessRight = "EDIT_OWN_INSTITUTION_PROJECTS";
        var input = IoUtils.stringFromResources(Path.of(PATCH_REQUEST_JSON));
        var gatewayResponse = sendQuery(input, legacyAccessRight);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAllowCreatorWithCorrectRoleToEditOwnProject() throws Exception {
        var fetchedProjectJson = cristinProjectWithCreatorData().toString();
        mockFetchResponse(fetchedProjectJson);

        var input = generateInputWithPayloadAndRequesterPersonCristinId(MANAGE_OWN_PROJECTS);
        handler.handleRequest(input, output, context);
        var response =  GatewayResponse.fromOutputStream(output, Void.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_NO_CONTENT));
    }

    @Test
    void shouldAllowProjectManagerWithCorrectRoleToEditTheirProjects() throws Exception {
        var fetchedProjectJson = cristinProjectWithManagerData().toString();
        mockFetchResponse(fetchedProjectJson);

        var input = generateInputWithPayloadAndRequesterPersonCristinId(MANAGE_OWN_PROJECTS);
        handler.handleRequest(input, output, context);
        var response =  GatewayResponse.fromOutputStream(output, Void.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_NO_CONTENT));
    }

    @Test
    void shouldNotAllowPersonWithCorrectRoleButIsNeitherProjectManagerOrCreatorToEditTheirProjects()
        throws Exception {
        var fetchedProjectJson = cristinProjectWithRegularParticipantData().toString();
        mockFetchResponse(fetchedProjectJson);

        var input = generateInputWithPayloadAndRequesterPersonCristinId(MANAGE_OWN_PROJECTS);
        handler.handleRequest(input, output, context);
        var response =  GatewayResponse.fromOutputStream(output, Void.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_FORBIDDEN));
    }

    @Test
    void shouldNotAllowPersonWithoutAccessRightOrRoleInProjectToEditRequestedProject() throws Exception {
        var fetchedProjectJson =
            cristinProjectWithParticipantRoles(ANOTHER_IDENTIFIER_THAN_USER, CRISTIN_PRO_MANAGER_CODE).toString();
        mockFetchResponse(fetchedProjectJson);

        var input = generateInputWithPayloadAndRequesterPersonCristinId(NO_ACCESS);
        handler.handleRequest(input, output, context);
        var response =  GatewayResponse.fromOutputStream(output, Void.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_FORBIDDEN));
    }

    @Test
    void shouldNotAllowPersonWhichIsCreatorAndManagerButWithoutRoleToEditTheirOwnProject() throws Exception {
        var fetchedProjectJson = cristinProjectWithManagerAndCreatorAsTheSame().toString();
        mockFetchResponse(fetchedProjectJson);

        var input = generateInputWithPayloadAndRequesterPersonCristinId(NO_ACCESS);
        handler.handleRequest(input, output, context);
        var response =  GatewayResponse.fromOutputStream(output, Void.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_FORBIDDEN));
    }

    private void mockFetchResponse(String fetchedProjectJson) throws IOException, InterruptedException {
        fetchApiClient = spy(fetchApiClient);
        doReturn(new HttpResponseFaker(fetchedProjectJson, HTTP_OK))
            .when(httpClientMockFetch).<String>send(any(),any());
    }

    private InputStream generateInputWithPayloadAndRequesterPersonCristinId(String accessRight)
        throws JsonProcessingException {

        var body = IoUtils.stringFromResources(Path.of(PATCH_REQUEST_JSON));
        var customerId = randomUri();
        var personCristinId = UriWrapper.fromUri(randomUri()).addChild(USER_IDENTIFIER).getUri();

        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withPersonCristinId(personCristinId)
                   .withAccessRights(customerId, accessRight)
                   .withPathParameters(validPath)
                   .build();
    }

    private String captureCristinPayload(UpdateCristinProjectApiClient apiClient)
        throws GatewayTimeoutException, FailedHttpRequestException {
        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).patch(any(), captor.capture());
        return captor.getValue();
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Void> sendQuery(String body) throws IOException {
        return sendQuery(body, MANAGE_OWN_PROJECTS);
    }

    private GatewayResponse<Void> sendQuery(String body, String accessRight) throws IOException {
        var input = createRequest(body, accessRight);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private InputStream createRequest(String body, String accessRight) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, accessRight)
                   .withPathParameters(validPath)
                   .build();
    }

    private GatewayResponse<Void> queryWithoutRequiredAccessRights() throws IOException {
        var input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(EMPTY_JSON)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private static Stream<Arguments> badRequestProvider() {
        return Stream.of(
            Arguments.of(FUNDING, notAnArray(FUNDING), format(MUST_BE_A_LIST, FUNDING)),
            Arguments.of(FUNDING, fundingWithoutRequiredFields(), FUNDING_MISSING_REQUIRED_FIELDS_OR_NOT_VALID),
            Arguments.of(LANGUAGE, languageNotSupported(), format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE)),
            Arguments.of(LANGUAGE, titlePresentButNotLanguage(), TITLE_MUST_HAVE_A_LANGUAGE),
            Arguments.of(LANGUAGE, languageNotAValidValue(), COULD_NOT_PARSE_LANGUAGE_FIELD),
            Arguments.of(KEYWORDS, missingRequiredFieldType(KEYWORDS), KEYWORDS_MISSING_REQUIRED_FIELD_TYPE),
            Arguments.of(PROJECT_CATEGORIES, missingRequiredFieldType(PROJECT_CATEGORIES),
                         PROJECT_CATEGORIES_MISSING_REQUIRED_FIELD_TYPE),
            Arguments.of(RELATED_PROJECTS, notAnArray(RELATED_PROJECTS), format(MUST_BE_A_LIST, RELATED_PROJECTS)),
            Arguments.of(RELATED_PROJECTS, notListOfStrings(), MUST_BE_A_LIST_OF_IDENTIFIERS),
            Arguments.of(ACADEMIC_SUMMARY, notADescription(ACADEMIC_SUMMARY),
                         format(NOT_A_VALID_KEY_VALUE_FIELD, ACADEMIC_SUMMARY)),
            Arguments.of(POPULAR_SCIENTIFIC_SUMMARY, notADescription(POPULAR_SCIENTIFIC_SUMMARY),
                         format(NOT_A_VALID_KEY_VALUE_FIELD, POPULAR_SCIENTIFIC_SUMMARY)),
            Arguments.of(METHOD, notADescription(METHOD),
                         format(NOT_A_VALID_KEY_VALUE_FIELD, METHOD)),
            Arguments.of(EQUIPMENT, notADescription(EQUIPMENT),
                         format(NOT_A_VALID_KEY_VALUE_FIELD, EQUIPMENT)),
            Arguments.of(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH, notAnOrganization(),
                         format(ILLEGAL_VALUE_FOR_PROPERTY, NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH)),
            Arguments.of(ALTERNATIVE_TITLES, notAListOfMaps(),
                         format(NOT_A_VALID_LIST_OF_KEY_VALUE_FIELDS, ALTERNATIVE_TITLES)),
            Arguments.of(WEB_PAGE, notAWebPage(), format(NOT_A_VALID_URI, WEB_PAGE)),
            Arguments.of(END_DATE, endDateInstant(EMPTY_STRING), format(ILLEGAL_VALUE_FOR_PROPERTY, END_DATE)),
            Arguments.of(END_DATE, endDateInstant(null), format(ILLEGAL_VALUE_FOR_PROPERTY, END_DATE)),
            Arguments.of(END_DATE, endDateInstant(randomString()), format(ILLEGAL_VALUE_FOR_PROPERTY, END_DATE))
        );
    }

    private static JsonNode endDateInstant(String fieldValue) {
        return OBJECT_MAPPER.createObjectNode().put(END_DATE, fieldValue);
    }

    private static JsonNode notAWebPage() {
        return OBJECT_MAPPER.createObjectNode().put(WEB_PAGE, NOT_SOME_WEBPAGE);
    }

    private static JsonNode notAnArray(String fieldName) {
        return OBJECT_MAPPER.createObjectNode().put(fieldName, randomString());
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

    private static JsonNode missingRequiredFieldType(String field) {
        var input = OBJECT_MAPPER.createObjectNode();
        var invalidElement = OBJECT_MAPPER.createObjectNode();
        invalidElement.put(randomString(), randomString());
        var array = OBJECT_MAPPER.createArrayNode();
        array.add(invalidElement);
        input.set(field, array);
        return input;
    }

    private static JsonNode notListOfStrings() {
        var input = OBJECT_MAPPER.createObjectNode();
        var objectField = OBJECT_MAPPER.createObjectNode().put(randomString(), randomString());
        var array = OBJECT_MAPPER.createArrayNode();
        array.add(objectField);
        input.set(RELATED_PROJECTS, array);
        return input;
    }

    private static JsonNode notADescription(String fieldName) {
        var input = OBJECT_MAPPER.createObjectNode();
        input.put(fieldName, randomString());
        return input;
    }

    private static JsonNode notAnOrganization() {
        var input = OBJECT_MAPPER.createObjectNode();
        var objectField = OBJECT_MAPPER.createObjectNode().put(randomString(), randomString());
        var array = OBJECT_MAPPER.createArrayNode();
        array.add(objectField);
        input.set(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH, array);
        return input;
    }

    private static JsonNode notAListOfMaps() {
        var input = OBJECT_MAPPER.createObjectNode();
        var array = OBJECT_MAPPER.createArrayNode();
        array.add(randomString());
        input.set(ALTERNATIVE_TITLES, array);
        return input;
    }

    private CristinProject basicCristinProject() {
        var cristinProject = new CristinProject();
        injectRequiredFields(cristinProject);
        return cristinProject;
    }

    private void injectRequiredFields(CristinProject cristinProject) {
        cristinProject.setCristinProjectId(randomInteger(999999).toString());
        cristinProject.setStatus(STATUS_ACTIVE);
        cristinProject.setTitle(Map.of(LANGUAGE_NORWEGIAN, randomString()));
    }

    private CristinProject cristinProjectWithCreatorData() {
        var cristinProject = basicCristinProject();
        var creator = new CristinPerson();
        creator.setCristinPersonId(USER_IDENTIFIER);
        cristinProject.setCreator(creator);

        return cristinProject;
    }

    private CristinProject cristinProjectWithManagerData() {
        return cristinProjectWithParticipantRoles(USER_IDENTIFIER, CRISTIN_PRO_MANAGER_CODE);
    }

    private CristinProject cristinProjectWithRegularParticipantData() {
        return cristinProjectWithParticipantRoles(USER_IDENTIFIER, CRISTIN_PRO_PARTICIPANT_CODE);
    }

    private CristinProject cristinProjectWithParticipantRoles(String userIdentifier, String roleCode) {
        var cristinProject = basicCristinProject();
        var participant = getParticipant(userIdentifier, roleCode);
        cristinProject.setParticipants(List.of(participant));

        return cristinProject;
    }

    private CristinPerson getParticipant(String userIdentifier, String roleCode) {
        var participant = new CristinPerson();
        participant.setCristinPersonId(userIdentifier);
        participant.setRoles(List.of(createRole(roleCode)));
        return participant;
    }

    private CristinRole createRole(String roleCode) {
        var cristinRole = new CristinRole();
        cristinRole.setRoleCode(roleCode);
        return cristinRole;
    }

    private CristinProject cristinProjectWithManagerAndCreatorAsTheSame() {
        var cristinProject = cristinProjectWithCreatorData();
        cristinProject.setParticipants(List.of(getParticipant(USER_IDENTIFIER, CRISTIN_PRO_MANAGER_CODE)));
        return cristinProject;
    }

}
