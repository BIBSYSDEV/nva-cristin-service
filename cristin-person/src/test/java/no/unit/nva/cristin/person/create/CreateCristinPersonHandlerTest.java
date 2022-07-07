package no.unit.nva.cristin.person.create;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.FIRST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.LAST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment.HASHTAG;
import static no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment.SLASH_DELIMITER;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.model.nva.Person.mapEmploymentsToCristinEmployments;
import static no.unit.nva.testutils.RandomDataGenerator.randomBoolean;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.cristin.CristinAffiliation;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonPost;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.exception.GatewayTimeoutException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class CreateCristinPersonHandlerTest {

    private static final String DEFAULT_IDENTITY_NUMBER = "07117631634";
    private static final String ANOTHER_IDENTITY_NUMBER = "12111739534";
    private static final String INVALID_IDENTITY_NUMBER = "11223344556";
    private static final String EMPTY_JSON = "{}";
    private static final String DUMMY_FIRST_NAME = randomString();
    private static final String DUMMY_LAST_NAME = randomString();
    private static final String DUMMY_CRISTIN_ID = "123456";
    private static final String SOME_UNIT_IDENTIFIER = "185.90.0.0";
    private static final String HEAD_ENGINEER_CODE = HASHTAG + "1087";

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
        String responseJson = OBJECT_MAPPER.writeValueAsString(dummyCristinPerson());
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);

        GatewayResponse<Person> gatewayResponse = sendQuery(dummyPerson());
        Person actual = gatewayResponse.getBodyObject(Person.class);

        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
        assertThat(actual.getNames().containsAll(dummyPerson().getNames()), equalTo(true));
    }

    @Test
    void shouldAcceptAdditionalNamesAndReturnCreated() throws IOException {
        Person input = dummyPersonWithAdditionalNames();
        GatewayResponse<Person> response = sendQuery(input);

        assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenClientPayloadIsMissingRequiredIdentifier() throws Exception {
        Person personWithMissingIdentity = new Person.Builder()
            .withNames(Set.of(new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME),
                              new TypedValue(LAST_NAME, DUMMY_LAST_NAME))).build();

        GatewayResponse<Person> gatewayResponse = sendQuery(personWithMissingIdentity);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                   containsString(CreateCristinPersonHandler.ERROR_MESSAGE_MISSING_IDENTIFIER));
    }

    @Test
    void shouldReturnBadRequestWhenClientPayloadIsEmpty() throws IOException {
        GatewayResponse<Person> gatewayResponse = sendQuery(null);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(CreateCristinPersonHandler.ERROR_MESSAGE_PAYLOAD_EMPTY));
    }

    @Test
    void shouldReturnForbiddenWhenClientIsMissingCredentials() throws IOException {
        GatewayResponse<Person> gatewayResponse = sendQueryWithoutAccessRights(dummyPerson());

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenIdentityNumberNotValid() throws IOException {
        GatewayResponse<Person> gatewayResponse = sendQuery(dummyPersonWithInvalidIdentityNumber());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                   containsString(CreateCristinPersonHandler.ERROR_MESSAGE_IDENTIFIER_NOT_VALID));
    }

    @Test
    void shouldReturnBadRequestWhenMissingRequiredNames() throws IOException {
        Person personWithMissingNames = new Person.Builder()
            .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DEFAULT_IDENTITY_NUMBER))).build();

        GatewayResponse<Person> gatewayResponse = sendQuery(personWithMissingNames);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                   containsString(CreateCristinPersonHandler.ERROR_MESSAGE_MISSING_REQUIRED_NAMES));
    }

    @Test
    void shouldReturnBadRequestWhenRepeatedIdentityNumber() throws IOException {

        Person dummyPersonWithRepeatedIdentityNumber = getPersonWithRepeatedIdentityNumber();

        GatewayResponse<Person> gatewayResponse = sendQuery(dummyPersonWithRepeatedIdentityNumber);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
                   containsString(CreateCristinPersonHandler.ERROR_MESSAGE_IDENTIFIERS_REPEATED));
    }

    @Test
    void shouldAllowUsersToCreateThemselvesInCristin() throws IOException, InterruptedException {
        String responseJson = OBJECT_MAPPER.writeValueAsString(dummyCristinPerson());
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CreateCristinPersonApiClient(httpClientMock);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        String personsOwnNin = ANOTHER_IDENTITY_NUMBER;
        Person input = injectPersonNinIntoInput(personsOwnNin);
        GatewayResponse<Person> gatewayResponse = sendQueryWithoutAccessRightsButWithPersonNin(input, personsOwnNin);

        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenIfUserTryingToCreateWithAnotherPersonNinThatDoesNotBelongToThemAndIsNotAuthorized()
        throws IOException {
        Person input = injectPersonNinIntoInput(DEFAULT_IDENTITY_NUMBER);
        GatewayResponse<Person> gatewayResponse =
            sendQueryWithoutAccessRightsButWithPersonNin(input, ANOTHER_IDENTITY_NUMBER);

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldHavePersonObjectWithEmploymentDataAddedAndSentToUpstream()
        throws IOException, GatewayTimeoutException, FailedHttpRequestException {
        apiClient = spy(apiClient);
        handler = new CreateCristinPersonHandler(apiClient, environment);
        var dummyPerson = dummyPerson();
        var dummyEmployments = randomEmployments();
        dummyPerson.setEmployments(dummyEmployments);
        final var gatewayResponse = sendQuery(dummyPerson);
        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).post(any(), captor.capture());
        var capturedCristinPerson = OBJECT_MAPPER.readValue(captor.getValue(), CristinPersonPost.class);
        var expectedCristinEmployments =
            mapEmploymentsToCristinEmployments(dummyEmployments);

        assertNotNull(capturedCristinPerson.getDetailedAffiliations());
        assertThat(capturedCristinPerson.getDetailedAffiliations().containsAll(expectedCristinEmployments),
                   equalTo(true));
        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
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

        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
        assertThat(actual.getAffiliations().contains(dummyCristinPersonAffiliation.toAffiliation()),
                   equalTo(true));
    }

    private Set<Employment> randomEmployments() {
        var employments = new HashSet<Employment>();
        IntStream.range(0, 3).mapToObj(i -> randomEmployment()).forEach(employments::add);
        return employments;
    }

    private Employment randomEmployment() {
        return new Employment.Builder()
                   .withOrganization(URI.create(randomUri() + SLASH_DELIMITER + SOME_UNIT_IDENTIFIER))
                   .withType(URI.create(randomUri() + HEAD_ENGINEER_CODE))
                   .withStartDate(randomInstant())
                   .withEndDate(randomInstant())
                   .withFullTimeEquivalentPercentage(randomInteger(100).doubleValue())
                   .build();
    }

    private CristinAffiliation randomCristinAffiliation() {
        var cristinAffiliation = new CristinAffiliation();
        cristinAffiliation.setActive(randomBoolean());
        cristinAffiliation.setRoleLabel(Map.of(randomString(), randomString()));
        cristinAffiliation.setUnit(CristinUnit.fromCristinUnitIdentifier(SOME_UNIT_IDENTIFIER));
        return cristinAffiliation;
    }

    private Person injectPersonNinIntoInput(String personNin) {
        Person input = dummyPerson();
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
        InputStream input = new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
                                .withBody(body)
                                .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private GatewayResponse<Person> sendQueryWithoutAccessRightsButWithPersonNin(Person body, String personNin)
        throws IOException {
        InputStream input = new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
                                .withBody(body)
                                .withPersonNin(personNin)
                                .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private GatewayResponse<Person> sendQuery(Person body) throws IOException {
        InputStream input = requestWithBody(body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private InputStream requestWithBody(Person body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
            .withBody(body)
            .withCustomerId(customerId)
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
        CristinPerson cristinPerson = new CristinPerson();
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
}
