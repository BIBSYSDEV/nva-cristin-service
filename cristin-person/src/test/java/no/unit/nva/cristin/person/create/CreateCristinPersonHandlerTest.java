package no.unit.nva.cristin.person.create;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static no.unit.nva.cristin.common.ErrorMessages.UPSTREAM_BAD_REQUEST_RESPONSE;
import static no.unit.nva.cristin.common.Utils.COULD_NOT_RETRIEVE_USER_CRISTIN_ORGANIZATION_IDENTIFIER;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.RandomPersonData.SOME_UNIT_IDENTIFIER;
import static no.unit.nva.cristin.person.RandomPersonData.randomEmployment;
import static no.unit.nva.cristin.person.RandomPersonData.randomEmployments;
import static no.unit.nva.cristin.person.create.CreateCristinPersonHandler.ERROR_MESSAGE_IDENTIFIERS_REPEATED;
import static no.unit.nva.cristin.person.create.CreateCristinPersonHandler.ERROR_MESSAGE_IDENTIFIER_NOT_VALID;
import static no.unit.nva.cristin.person.create.CreateCristinPersonHandler.ERROR_MESSAGE_MISSING_REQUIRED_NAMES;
import static no.unit.nva.cristin.person.create.CreateCristinPersonHandler.ERROR_MESSAGE_PAYLOAD_EMPTY;
import static no.unit.nva.cristin.person.create.PersonNviValidator.INVALID_PERSON_ID;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.FIRST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.LAST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.model.nva.Person.mapEmploymentsToCristinEmployments;
import static no.unit.nva.testutils.RandomDataGenerator.randomBoolean;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.apigateway.RequestInfoConstants.BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.cristin.CristinAffiliation;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.PersonSummary;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.exception.GatewayTimeoutException;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.zalando.problem.Problem;


public class CreateCristinPersonHandlerTest {

    private static final String DEFAULT_IDENTITY_NUMBER = "07117631634";
    private static final String ANOTHER_IDENTITY_NUMBER = "12111739534";
    private static final String INVALID_IDENTITY_NUMBER = "11223344556";
    private static final String EMPTY_JSON = "{}";
    private static final String DUMMY_FIRST_NAME = randomString();
    private static final String DUMMY_LAST_NAME = randomString();
    private static final String DUMMY_CRISTIN_ID = "123456";
    private static final String ANOTHER_ORGANIZATION =
        "https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0";
    private static final String ONE_ORGANIZATION = "https://api.dev.nva.aws.unit.no/cristin/organization/20754.0.0.0";
    private static final String LOG_MESSAGE_FOR_IDENTIFIERS = "Client has Cristin identifier 123456 from organization "
                                                              + "20754.0.0.0";
    public static final String VERIFIED_BY_ID_URI_NVI = "https://api.dev.nva.aws.unit.no/cristin/person/1234";
    public static final String VERIFIED_BY_NVI_CRISTIN_IDENTIFIER = "1234";
    public static final String ONE_ORGANIZATION_IDENTIFIER_LAST_PART = "20754.0.0.0";
    public static final String ONE_ORGANIZATION_CRISTIN_INSTNR = "20754";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private CreateCristinPersonApiClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private CreateCristinPersonHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new CreateCristinPersonHandler(apiClient, environment);
    }

    @Test
    void shouldCreateAndReturnPersonWhenClientSendsValidPayload() throws IOException, InterruptedException {
        var responseJson = OBJECT_MAPPER.writeValueAsString(dummyCristinPerson());
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(dummyPerson());
        var actual = gatewayResponse.getBodyObject(Person.class);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
        assertThat(actual.getNames().containsAll(dummyPerson().getNames()), equalTo(true));
    }

    @Test
    void shouldAcceptAdditionalNamesAndReturnCreated() throws IOException {
        var input = dummyPersonWithAdditionalNames();
        var response = sendQuery(input);

        assertEquals(HTTP_CREATED, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenClientPayloadIsEmpty() throws IOException {
        var gatewayResponse = sendQuery(null);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_PAYLOAD_EMPTY));
    }

    @Test
    void shouldReturnForbiddenWhenClientIsMissingCredentials() throws IOException {
        var gatewayResponse = sendQueryWithoutAccessRights(dummyPerson());

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenIdentityNumberNotValid() throws IOException {
        var gatewayResponse = sendQuery(dummyPersonWithInvalidIdentityNumber());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_IDENTIFIER_NOT_VALID));
    }

    @Test
    void shouldReturnBadRequestWhenMissingRequiredNames() throws IOException {
        var personWithMissingNames =
            new Person.Builder()
                .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DEFAULT_IDENTITY_NUMBER)))
                .build();
        var gatewayResponse = sendQuery(personWithMissingNames);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_MISSING_REQUIRED_NAMES));
    }

    @Test
    void shouldReturnBadRequestWhenRepeatedIdentityNumber() throws IOException {
        var dummyPersonWithRepeatedIdentityNumber = getPersonWithRepeatedIdentityNumber();
        var gatewayResponse = sendQuery(dummyPersonWithRepeatedIdentityNumber);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_IDENTIFIERS_REPEATED));
    }

    @Test
    void shouldAllowUsersToCreateThemselvesInCristin() throws IOException, InterruptedException {
        var responseJson = OBJECT_MAPPER.writeValueAsString(dummyCristinPerson());
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var personsOwnNin = ANOTHER_IDENTITY_NUMBER;
        var input = injectPersonNinIntoInput(personsOwnNin);
        var gatewayResponse =
            sendQueryWithoutAccessRightsButWithPersonNin(input, personsOwnNin);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenIfUserTryingToCreateWithAnotherPersonNinThatDoesNotBelongToThemAndIsNotAuthorized()
        throws IOException {
        var input = injectPersonNinIntoInput(DEFAULT_IDENTITY_NUMBER);
        var gatewayResponse =
            sendQueryWithoutAccessRightsButWithPersonNin(input, ANOTHER_IDENTITY_NUMBER);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenIfUserTryingToCreateSomeoneOtherThanThemselvesWithNinSetToNull()
        throws IOException {
        var input = injectPersonNinIntoInput(null);
        var gatewayResponse =
            sendQueryWithoutAccessRightsButWithPersonNin(input, ANOTHER_IDENTITY_NUMBER);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "hello", "1234"})
    void shouldThrowForbiddenIfUserTryingToCreateSomeoneOtherThanThemselvesWithNinSetToInvalidValue(String nin)
        throws IOException {
        var input = injectPersonNinIntoInput(nin);
        var gatewayResponse =
            sendQueryWithoutAccessRightsButWithPersonNin(input, ANOTHER_IDENTITY_NUMBER);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldHavePersonObjectWithEmploymentDataAddedAndSentToUpstream()
        throws IOException, GatewayTimeoutException, FailedHttpRequestException {
        apiClient = spy(apiClient);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var dummyPerson = dummyPerson();
        var dummyEmployments = randomEmployments();
        dummyPerson.setEmployments(dummyEmployments);
        final var gatewayResponse =
            sendQueryWhileMockingCristinOrgIdOfClient(dummyPerson, URI.create(ONE_ORGANIZATION));
        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).post(any(), captor.capture(), any());
        var capturedCristinPerson = OBJECT_MAPPER.readValue(captor.getValue(), CristinPerson.class);
        var expectedCristinEmployments =
            mapEmploymentsToCristinEmployments(dummyEmployments);
        Objects.requireNonNull(expectedCristinEmployments);

        assertNotNull(capturedCristinPerson.getDetailedAffiliations());
        assertThat(capturedCristinPerson.getDetailedAffiliations().containsAll(expectedCristinEmployments),
                   equalTo(true));
        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReceiveMinimalEmploymentDataInResponseWhenEmploymentDataIsReturnedFromUpstreamAsPartOfPersonCreation()
        throws IOException, InterruptedException {
        var dummyCristinPerson = dummyCristinPerson();
        var dummyCristinPersonAffiliation = randomCristinAffiliation();
        dummyCristinPerson.setAffiliations(List.of(dummyCristinPersonAffiliation));
        var responseJson = OBJECT_MAPPER.writeValueAsString(dummyCristinPerson);
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(dummyPerson());
        var actual = gatewayResponse.getBodyObject(Person.class);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
        assertThat(actual.getAffiliations().contains(dummyCristinPersonAffiliation.toAffiliation()),
                   equalTo(true));
    }

    @Test
    void shouldThrowBadRequestOnInvalidEmploymentDataForPerson() throws IOException {
        var dummyPerson = dummyPerson();
        var dummyEmployment = randomEmployment();
        dummyEmployment.setType(null);
        dummyPerson.setEmployments(Set.of(dummyEmployment));
        final var gatewayResponse = sendQuery(dummyPerson);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldDefaultToEmptyEmploymentSetToAuthorizedClientWhenFieldNotPresentInUpstreamIndicatingNoEmployments()
        throws IOException, InterruptedException {

        var responseJson = OBJECT_MAPPER.writeValueAsString(dummyCristinPerson());
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQuery(dummyPerson());
        var actual = gatewayResponse.getBodyObject(Person.class);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
        assertThat(actual.getEmployments(), equalTo(Collections.emptySet()));
    }

    @Test
    void shouldReturnForbiddenWhenUserActingAsThemselvesAndNotAdminTryToCreateTheirOwnEmployments() throws IOException {
        var dummyEmployments = randomEmployments();
        var personsOwnNin = ANOTHER_IDENTITY_NUMBER;
        var input = injectPersonNinIntoInput(personsOwnNin);
        input.setEmployments(dummyEmployments);
        var gatewayResponse = sendQueryWithoutAccessRightsButWithPersonNin(input, personsOwnNin);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenMissingTopLevelCristinOrgIdFromRequestInfo() throws IOException {
        var dummyPerson = dummyPerson();
        var dummyEmployment = randomEmployment();
        dummyEmployment.setOrganization(URI.create(ANOTHER_ORGANIZATION));
        dummyPerson.setEmployments(Set.of(dummyEmployment));
        final var gatewayResponse =
            sendQueryWhileMockingCristinOrgIdOfClient(dummyPerson, URI.create(EMPTY_STRING));

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(),
                   containsString(COULD_NOT_RETRIEVE_USER_CRISTIN_ORGANIZATION_IDENTIFIER));
    }

    @Test
    void shouldBeAllowedToCreateEmploymentsAtAllInstitutionsWhenIsInternalBackend() throws IOException {
        var dummyPerson = dummyPerson();
        var dummyEmployment = randomEmployment();
        dummyEmployment.setOrganization(URI.create(ANOTHER_ORGANIZATION));
        dummyPerson.setEmployments(Set.of(dummyEmployment));
        final var gatewayResponse = sendQueryWhileActingAsInternalBackend(dummyPerson);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldAllowEmptyListOfEmploymentsWhenUserActingAsThemselvesTryToCreateTheirOwnUser() throws IOException {
        var personsOwnNin = ANOTHER_IDENTITY_NUMBER;
        var input = injectPersonNinIntoInput(personsOwnNin);
        input.setEmployments(Collections.emptySet());
        var gatewayResponse = sendQueryWithoutAccessRightsButWithPersonNin(input, personsOwnNin);

        assertThat(input.getEmployments(), equalTo(Collections.emptySet()));
        assertThat(input.getEmployments().size(), equalTo(0));
        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldLogClientSpecificIdentifiersWhenDoingAuthorizedRequests() throws IOException {
        try (var outputStreamCaptor = new ByteArrayOutputStream()) {
            var currentPrintSteam = System.out;
            try (var newPrintStream = new PrintStream(outputStreamCaptor)) {
                System.setOut(newPrintStream);
                var response = sendQueryWhileMockingIdentifiersUsedForLogging(dummyPerson());
                assertEquals(HTTP_CREATED, response.getStatusCode());
            }
            System.setOut(currentPrintSteam);
            assertThat(outputStreamCaptor.toString(), Matchers.containsString(LOG_MESSAGE_FOR_IDENTIFIERS));
        }
    }

    @Test
    void shouldReturnResponseBodyFromUpstreamToClientWhenUpstreamReturnsBadRequest()
        throws IOException, InterruptedException {
        var responseBody = "This is a bad request";
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseBody, 400));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var gatewayResponse = sendQueryReturningProblemJson(dummyPerson());
        var actual = gatewayResponse.getBodyObject(Problem.class).getDetail();

        assertThat(actual, CoreMatchers.equalTo(UPSTREAM_BAD_REQUEST_RESPONSE + responseBody));
    }

    @Test
    void shouldAddPersonNviDataToCristinJsonWhenPresentInInput() throws Exception {
        apiClient = spy(apiClient);
        handler = new CreateCristinPersonHandler(apiClient, environment);

        var dummyPerson = dummyPersonNviVerified(dummyNviData());

        final var actual = sendQueryWhileMockingCristinOrgIdOfClient(dummyPerson, URI.create(ONE_ORGANIZATION));

        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).post(any(), captor.capture(), eq(ONE_ORGANIZATION_CRISTIN_INSTNR));
        var capturedCristinPerson = OBJECT_MAPPER.readValue(captor.getValue(), CristinPerson.class);
        var personNviFromCapture = capturedCristinPerson.getPersonNvi();

        assertThat(personNviFromCapture.verifiedBy().cristinPersonId(), equalTo(VERIFIED_BY_NVI_CRISTIN_IDENTIFIER));
        assertThat(personNviFromCapture.verifiedAt().unit().getCristinUnitId(), equalTo(
            ONE_ORGANIZATION_IDENTIFIER_LAST_PART));
        assertThat(actual.getStatusCode(), equalTo(HTTP_CREATED));
    }

    @Test
    void shouldThrowBadRequestWhenPersonNviDataSuppliedIsInvalid() throws Exception {
        apiClient = spy(apiClient);
        handler = new CreateCristinPersonHandler(apiClient, environment);

        var dummyPerson = dummyPersonNviVerified(invalidDummyNviData());

        var actual = sendQuery(dummyPerson);

        assertThat(actual.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
        assertThat(actual.getBody(), containsString(INVALID_PERSON_ID));
    }

    @Test
    void shouldReturnCreatedWhenClientPayloadIsMissingNinButIsUserAdmin() throws Exception {
        var personWithMissingIdentity = new Person.Builder()
                                            .withNames(Set.of(new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                                                              new TypedValue(LAST_NAME, DUMMY_LAST_NAME)))
                                            .build();
        var gatewayResponse = sendQuery(personWithMissingIdentity);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldHaveCorrectDataFromInputInUpstreamJson() throws Exception {
        apiClient = spy(apiClient);
        handler = new CreateCristinPersonHandler(apiClient, environment);

        var inputJson = readFile("nvaApiCreatePersonRequestMoreFields.json");
        var input = OBJECT_MAPPER.readValue(inputJson, Person.class);
        var actual = sendQuery(input);

        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).post(any(), captor.capture());
        var expectedCristinRequest = readFile("cristinCreatePersonRequest.json");

        JSONAssert.assertEquals(expectedCristinRequest, captor.getValue(), JSONCompareMode.LENIENT);
        assertThat(actual.getStatusCode(), equalTo(HTTP_CREATED));
    }

    private static String readFile(String file) {
        return IoUtils.stringFromResources(Path.of(file));
    }

    private CristinAffiliation randomCristinAffiliation() {
        var cristinAffiliation = new CristinAffiliation();
        cristinAffiliation.setActive(randomBoolean());
        cristinAffiliation.setRoleLabel(Map.of(randomString(), randomString()));
        cristinAffiliation.setUnit(CristinUnit.fromCristinUnitIdentifier(SOME_UNIT_IDENTIFIER));
        return cristinAffiliation;
    }

    private Person injectPersonNinIntoInput(String personNin) {
        var input = dummyPerson();
        input.setIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, personNin)));
        return input;
    }

    private Person getPersonWithRepeatedIdentityNumber() {
        return new Person.Builder()
                   .withNames(Set.of(
                       new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                       new TypedValue(LAST_NAME, DUMMY_LAST_NAME)))
                   .withIdentifiers(Set.of(
                       new TypedValue(NATIONAL_IDENTITY_NUMBER, DEFAULT_IDENTITY_NUMBER),
                       new TypedValue(NATIONAL_IDENTITY_NUMBER, ANOTHER_IDENTITY_NUMBER)))
                   .build();
    }

    private GatewayResponse<Person> sendQueryWithoutAccessRights(Person body) throws IOException {
        var input = new HandlerRequestBuilder<Person>(OBJECT_MAPPER).withBody(body)
                                .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private GatewayResponse<Person> sendQueryWithoutAccessRightsButWithPersonNin(Person body, String personNin)
        throws IOException {
        var input = new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
                                .withBody(body)
                                .withPersonNin(personNin)
                                .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private GatewayResponse<Person> sendQuery(Person body) throws IOException {
        var input = requestWithBody(body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private GatewayResponse<Problem> sendQueryReturningProblemJson(Person body) throws IOException {
        try (var input = requestWithBody(body)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Problem.class);
    }

    private InputStream requestWithBody(Person body) throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
            .withBody(body)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, EDIT_OWN_INSTITUTION_USERS)
            .build();
    }

    private Person dummyPerson() {
        return new Person.Builder()
            .withNames(Set.of(
                new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                new TypedValue(LAST_NAME, DUMMY_LAST_NAME)))
            .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DEFAULT_IDENTITY_NUMBER)))
            .build();
    }

    private Person dummyPersonWithInvalidIdentityNumber() {
        return new Person.Builder()
            .withNames(Set.of(
                new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                new TypedValue(LAST_NAME, DUMMY_LAST_NAME)))
            .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, INVALID_IDENTITY_NUMBER)))
            .build();
    }

    private CristinPerson dummyCristinPerson() {
        var cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(DUMMY_CRISTIN_ID);
        cristinPerson.setFirstName(DUMMY_FIRST_NAME);
        cristinPerson.setSurname(DUMMY_LAST_NAME);
        return cristinPerson;
    }

    private Person dummyPersonWithAdditionalNames() {
        return new Person.Builder()
            .withNames(Set.of(
                new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                new TypedValue(LAST_NAME, DUMMY_LAST_NAME),
                new TypedValue(PREFERRED_FIRST_NAME, randomString()),
                new TypedValue(PREFERRED_LAST_NAME, randomString())))
            .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DEFAULT_IDENTITY_NUMBER)))
            .build();
    }

    private GatewayResponse<Person> sendQueryWhileMockingCristinOrgIdOfClient(Person body, URI cristinOrgId)
        throws IOException {
        var input = requestWithBodyAndMockedCristinOrgId(body, cristinOrgId);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private InputStream requestWithBodyAndMockedCristinOrgId(Person body, URI cristinOrgId)
        throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withTopLevelCristinOrgId(cristinOrgId)
                   .withAccessRights(customerId, EDIT_OWN_INSTITUTION_USERS)
                   .build();
    }

    private GatewayResponse<Person> sendQueryWhileActingAsInternalBackend(Person body)
        throws IOException {
        try (var input = requestWithBodyActingAsInternalBackend(body)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private InputStream requestWithBodyActingAsInternalBackend(Person body)
        throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withScope(BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE)
                   .build();
    }

    private GatewayResponse<Person> sendQueryWhileMockingIdentifiersUsedForLogging(Person body)
        throws IOException {
        final var customerId = randomUri();
        try (var input = new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
                             .withBody(body)
                             .withCurrentCustomer(customerId)
                             .withPersonCristinId(UriWrapper.fromUri(randomUri()).addChild(DUMMY_CRISTIN_ID).getUri())
                             .withTopLevelCristinOrgId(UriWrapper.fromUri(ONE_ORGANIZATION).getUri())
                             .withAccessRights(customerId, EDIT_OWN_INSTITUTION_USERS)
                             .build()) {
            handler.handleRequest(input, output, context);
            return GatewayResponse.fromOutputStream(output, Person.class);
        }
    }

    private Person dummyPersonNviVerified(PersonNvi nviData) {
        return new Person.Builder()
                   .withNames(Set.of(
                       new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                       new TypedValue(LAST_NAME, DUMMY_LAST_NAME)))
                   .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DEFAULT_IDENTITY_NUMBER)))
                   .withNvi(nviData)
                   .build();
    }

    private PersonNvi dummyNviData() {
        var verifiedBy = new PersonSummary(URI.create(VERIFIED_BY_ID_URI_NVI), null, null);
        var verifiedAt = new Organization.Builder().withId(URI.create(ONE_ORGANIZATION)).build();
        return new PersonNvi(verifiedBy, verifiedAt, null);
    }

    private PersonNvi invalidDummyNviData() {
        var verifiedBy = new PersonSummary(null, null, null);
        var verifiedAt = new Organization.Builder().withId(URI.create(ONE_ORGANIZATION)).build();
        return new PersonNvi(verifiedBy, verifiedAt, null);
    }

}
