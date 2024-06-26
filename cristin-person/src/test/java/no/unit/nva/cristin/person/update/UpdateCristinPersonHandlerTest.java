package no.unit.nva.cristin.person.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.zalando.problem.Problem;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.UPSTREAM_BAD_REQUEST_RESPONSE;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.RandomPersonData.randomEmployment;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AWARDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COUNTRIES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.COULD_NOT_PARSE_AWARDS_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.COULD_NOT_PARSE_COUNTRIES_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.COULD_NOT_PARSE_EMPLOYMENT_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.COULD_NOT_PARSE_KEYWORD_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.COULD_NOT_PARSE_NVI_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.FIELD_CAN_NOT_BE_ERASED;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.MUST_HAVE_A_VALID_ORGANIZATION_IDENTIFIER;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.MUST_HAVE_A_VALID_PERSON_IDENTIFIER;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.ORCID_IS_NOT_VALID;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.RESERVED_MUST_BE_BOOLEAN;
import static no.unit.nva.cristin.person.update.UpdateCristinPersonHandler.ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.apigateway.RequestInfoConstants.BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdateCristinPersonHandlerTest {

    private static final String NVI_JSON_AT_185 = IoUtils.stringFromResources(Path.of("nvaApiPersonNviData.json"));
    private static final String PERSON_CRISTIN_ID = "123456";
    private static final URI PERSON_CRISTIN_ID_URI = UriWrapper.fromUri(randomUri()).addChild(PERSON_CRISTIN_ID)
                                                         .getUri();
    private static final Map<String, String> validPath = Map.of(PERSON_ID, PERSON_CRISTIN_ID);
    private static final String VALID_ORCID = "1234-1234-1234-1234";
    private static final String INVALID_ORCID = "1234";
    private static final String SOME_TEXT = "Hello";
    private static final String UNSUPPORTED_FIELD = "unsupportedField";
    private static final String INVALID_IDENTIFIER = "hello";
    private static final String IDENTIFIER_NOT_MATCHING_COGNITO = "555666";
    private static final String SOME_ORGANIZATION = "185.90.0.0";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateCristinPersonHandler handler;
    private UpdateCristinPersonApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        apiClient = new UpdateCristinPersonApiClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdateCristinPersonHandler(apiClient, environment);
    }

    @Test
    void shouldReturnNoContentResponseWhenCallingHandlerWithValidJson() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);
        jsonObject.put(FIRST_NAME, randomString());
        jsonObject.put(PREFERRED_FIRST_NAME, randomString());
        jsonObject.putNull(PREFERRED_LAST_NAME);

        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenClientIsNotAuthenticated() throws IOException {
        var gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws IOException {
        var gatewayResponse = sendQuery(validPath, null);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnOkNoContentWhenOrcidIsPresentAndNull() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putNull(ORCID);

        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenOrcidHasInvalidValue() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, INVALID_ORCID);

        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ORCID_IS_NOT_VALID));
    }

    @Test
    void shouldReturnBadRequestWhenPrimaryNameIsNull() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putNull(FIRST_NAME);

        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(String.format(FIELD_CAN_NOT_BE_ERASED, FIRST_NAME)));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsNotBoolean() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(RESERVED, SOME_TEXT);

        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_MUST_BE_BOOLEAN));
    }

    @Test
    void shouldReturnBadRequestWhenNoSupportedFieldsArePresent() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(UNSUPPORTED_FIELD, randomString());

        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidIdentifier() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);

        var gatewayResponse = sendQuery(Map.of(PERSON_ID, INVALID_IDENTIFIER),
                                        jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.invalidPathParameterMessage(PERSON_ID)));
    }

    @Test
    void shouldReturnNoContentResponseWhenUserIsUpdatingTheirOwnPersonDataThatTheyAreAllowedToUpdate()
        throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);
        var gatewayResponse = queryWithPersonCristinIdButNoAccessRights(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenUsersWantToUpdateTheirOwnPersonDataButSendsFieldsTheyAreNotAllowedToUpdate()
        throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(FIRST_NAME, randomString());
        jsonObject.put(LAST_NAME, randomString());
        jsonObject.put(RESERVED, true);
        var gatewayResponse = queryWithPersonCristinIdButNoAccessRights(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }

    @Test
    void shouldThrowBadRequestWhenPathPersonIdentifierNotMatchingCognitoPersonIdentifier()
        throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);
        var gatewayResponse =
            queryWithPersonCristinIdButNoAccessRights(Map.of(PERSON_ID, IDENTIFIER_NOT_MATCHING_COGNITO),
                                                      jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH));
    }

    @Test
    void shouldThrowBadRequestWhenPersonEmploymentDataNotValid() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        var invalidData = randomString();
        jsonObject.put(EMPLOYMENTS, invalidData);
        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(COULD_NOT_PARSE_EMPLOYMENT_FIELD));
    }

    @Test
    void shouldAllowEmptyListOfEmployments() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putArray(EMPLOYMENTS);
        var gatewayResponse =
            sendQueryAsInstAdminWithSomeOrgId(jsonObject.toString(), getDummyOrgUri185());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAllowValidEmploymentDataWhenInstAdminAndHasAnyCristinOrgIdAsValidationIsDoneUpstream()
        throws IOException {
        var employment = randomEmployment();
        var node = OBJECT_MAPPER.readTree(employment.toString());
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putArray(EMPLOYMENTS).add(node);
        var gatewayResponse =
            sendQueryAsInstAdminWithSomeOrgId(jsonObject.toString(), employment.getOrganization());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAllowAnyEmploymentsWhenInternalBackend() throws IOException {
        var employment = randomEmployment();
        var node = OBJECT_MAPPER.readTree(employment.toString());
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putArray(EMPLOYMENTS).add(node);
        var gatewayResponse = sendQueryAsInternalBackend(jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnResponseBodyFromUpstreamToClientWhenUpstreamReturnsBadRequest()
        throws IOException, InterruptedException {

        var responseBody = "This is a bad request";
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseBody, 400));
        apiClient = new UpdateCristinPersonApiClient(httpClientMock);
        handler = new UpdateCristinPersonHandler(apiClient, environment);
        var input = OBJECT_MAPPER.createObjectNode();
        input.put(FIRST_NAME, randomString());
        var gatewayResponse = sendQueryReturningProblemJson(input.toString());
        var actual = gatewayResponse.getBodyObject(Problem.class).getDetail();

        assertThat(actual, equalTo(UPSTREAM_BAD_REQUEST_RESPONSE + responseBody));
    }

    @Test
    void shouldThrowBadRequestWhenPersonKeywordsNotValid() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        var invalidData = randomString();
        jsonObject.put(KEYWORDS, invalidData);
        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(COULD_NOT_PARSE_KEYWORD_FIELD));
    }

    @ParameterizedTest
    @MethodSource("personNviBadRequestProvider")
    void shouldThrowBadRequestWhenPersonNviNotValid(String json, String errorMsg) throws IOException {
        var gatewayResponse = sendQuery(validPath, json);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(errorMsg));
    }

    @Test
    void shouldReturnNoContentWhenPersonNviHasValidContent() throws IOException {
        var gatewayResponse = sendQuery(validPath, NVI_JSON_AT_185);

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldFilterOutNviWhenClientNotAllowedToUpdateAtThatOrganizationForNviVerification() throws Exception {
        apiClient = spy(apiClient);
        handler = new UpdateCristinPersonHandler(apiClient, environment);

        final var actual = sendQueryAsInstAdminWithSomeOrgId(NVI_JSON_AT_185, getDummyOrgUri2012());

        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).patch(any(), captor.capture());
        var capturedCristinPerson = OBJECT_MAPPER.readValue(captor.getValue(), CristinPerson.class);
        var personNviFromCapture = capturedCristinPerson.getPersonNvi();

        assertThat(personNviFromCapture, is(nullValue()));
        assertEquals(HTTP_NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void shouldNotFilterOutNviWhenClientIsAllowedToUpdateAtThatOrganizationForNviVerification() throws Exception {
        apiClient = spy(apiClient);
        handler = new UpdateCristinPersonHandler(apiClient, environment);

        final var actual = sendQueryAsInstAdminWithSomeOrgId(NVI_JSON_AT_185, getDummyOrgUri185());

        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).patch(any(), captor.capture(), any());
        var capturedCristinPerson = OBJECT_MAPPER.readValue(captor.getValue(), CristinPerson.class);
        var personNviFromCapture = capturedCristinPerson.getPersonNvi();

        var outputUnitId = personNviFromCapture.verifiedAt().unit().getCristinUnitId();
        var instNrFromOutput = Utils.removeUnitPartFromIdentifierIfPresent(outputUnitId);
        var clientInstitutionId = "185";

        assertThat(instNrFromOutput, equalTo(clientInstitutionId));
        assertEquals(HTTP_NO_CONTENT, actual.getStatusCode());
    }

    @Test
    void shouldCallApiClientWithAddedInstitutionHeaderWhenPayloadHavingNviData() throws Exception {
        apiClient = spy(apiClient);
        handler = new UpdateCristinPersonHandler(apiClient, environment);

        sendQueryAsInstAdminWithSomeOrgId(NVI_JSON_AT_185, getDummyOrgUri185());
        var clientInstitutionId = "185";
        var captor = ArgumentCaptor.forClass(String.class);

        verify(apiClient).patch(any(), captor.capture(), eq(clientInstitutionId));
    }

    @Test
    void shouldThrowBadRequestWhenPersonCountriesNotValid() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        var invalidData = randomString();
        jsonObject.put(COUNTRIES, invalidData);
        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(COULD_NOT_PARSE_COUNTRIES_FIELD));
    }

    @Test
    void shouldThrowBadRequestWhenPersonAwardsNotValid() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        var invalidData = randomString();
        jsonObject.put(AWARDS, invalidData);
        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(COULD_NOT_PARSE_AWARDS_FIELD));
    }

    @Test
    void shouldAllowEmptyListOfPersonAwards() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putArray(AWARDS);
        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAllowNullableAwards() throws IOException {
        var jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putNull(AWARDS);
        var gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    private static Stream<Arguments> personNviBadRequestProvider() {
        return Stream.of(
            Arguments.of(
                """
                {
                  "nvi": "hello"
                }
                """,
                COULD_NOT_PARSE_NVI_FIELD
            ),
            Arguments.of(
                """
                {
                  "nvi": {
                    "verifiedAt": {
                      "hello": {
                        "en": "world"
                      }
                    },
                    "verifiedDate": "2023-10-03T15:00:30Z"
                  }
                }
                """,
                MUST_HAVE_A_VALID_ORGANIZATION_IDENTIFIER
            ),
            Arguments.of(
                """
                {
                  "nvi": {
                    "verifiedBy": {
                      "id": ""
                    },
                    "verifiedAt": {
                      "id": "https://api.dev.nva.aws.unit.no/cristin/organization/185.11.0.0"
                    },
                    "verifiedDate": "2023-10-03T15:00:30Z"
                  }
                }
                """,
                MUST_HAVE_A_VALID_PERSON_IDENTIFIER
            )
        );
    }

    private URI getDummyOrgUri185() {
        return UriWrapper.fromUri(randomUri()).addChild(SOME_ORGANIZATION).getUri();
    }

    private URI getDummyOrgUri2012() {
        return UriWrapper.fromUri(randomUri()).addChild(randomString()).addChild("2012").getUri();
    }

    private GatewayResponse<Void> sendQueryAsInstAdminWithSomeOrgId(String body, URI orgId) throws IOException {
        try (var input = createRequest(body, null, orgId, MANAGE_OWN_AFFILIATION)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private GatewayResponse<Void> sendQueryAsInternalBackend(String body) throws IOException {
        try (var input = createRequest(body, BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE, null)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private GatewayResponse<Void> sendQuery(Map<String, String> pathParam, String body) throws IOException {
        try (var input = createRequest(pathParam, body)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private GatewayResponse<Problem> sendQueryReturningProblemJson(String body) throws IOException {
        try (var input = createRequest(validPath, body)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Problem.class);
    }

    private InputStream createRequest(String body, String scope, URI orgId,
                                      AccessRight... accessRight)
        throws JsonProcessingException {

        var customerId = randomUri();
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                   .withBody(body)
                   .withPathParameters(validPath)
                   .withCurrentCustomer(customerId)
                   .withTopLevelCristinOrgId(Optional.ofNullable(orgId).orElse(URI.create(EMPTY_STRING)))
                   .withScope(scope)
                   .withAccessRights(customerId, accessRight)
                   .build();
    }

    private InputStream createRequest(Map<String, String> pathParam, String body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(body)
            .withPathParameters(pathParam)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, MANAGE_OWN_AFFILIATION)
            .build();
    }

    private GatewayResponse<Void> queryWithoutRequiredAccessRights() throws IOException {
        try (var input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                             .withBody(EMPTY_JSON)
                             .withPathParameters(validPath)
                             .build()) {
            handler.handleRequest(input, output, context);
        }

        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private GatewayResponse<Void> queryWithPersonCristinIdButNoAccessRights(Map<String, String> pathParameters,
                                                                            String body) throws IOException {
        try (var input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                             .withBody(body)
                             .withPathParameters(pathParameters)
                             .withPersonCristinId(PERSON_CRISTIN_ID_URI)
                             .build()) {
            handler.handleRequest(input, output, context);
        }

        return GatewayResponse.fromOutputStream(output, Void.class);
    }

}
