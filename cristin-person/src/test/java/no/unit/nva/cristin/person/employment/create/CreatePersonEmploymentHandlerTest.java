package no.unit.nva.cristin.person.employment.create;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.nva.common.IdCreatedLogger.CLIENT_CREATED_RESOURCE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.RandomPersonData.randomEmployment;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
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
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Map;
import no.unit.nva.common.IdCreatedLogger;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.RequestInfoConstants;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreatePersonEmploymentHandlerTest {

    private static final String SOME_PERSON_ID = "112233";
    private static final Map<String, String> validPath = Map.of(PERSON_ID, SOME_PERSON_ID);
    private static final String EMPTY_JSON = "{}";
    private static final String validJson =
        IoUtils.stringFromResources(Path.of("nvaApiCreateEmploymentRequest.json"));
    private static final String INVALID_URI = "https://example.org/hello";
    private static final URI TOP_ORG_ID = UriWrapper.fromUri(randomUri()).addChild("185.90.0.0").getUri();

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private CreatePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 201));
        var apiClient = new CreatePersonEmploymentClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new CreatePersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldReturnStatusCreatedWhenSendingValidPayload() throws IOException {
        var employment = sampleEmployment();
        var gatewayResponse = sendQueryWithCristinOrgId(employment);

        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    private Employment sampleEmployment() throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(validJson, Employment.class);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        var gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldAllowBackendClientsToCreatePersonEmployments() throws IOException {
        var employment = sampleEmployment();
        var gatewayResponse = sendBackendClientQuery(employment);
        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());

    }

    private GatewayResponse<Employment> sendBackendClientQuery(Employment employment) throws IOException {
        var query = new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
            .withBody(employment)
            .withPathParameters(Map.of(PERSON_ID,randomIntegerAsString()))
            .withScope(RequestInfoConstants.BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE)
            .build();
        handler.handleRequest(query,output,context);
        return GatewayResponse.fromOutputStream(output, Employment.class);
    }

    @Test
    void shouldThrowBadRequestWhenInvalidPositionCode() throws IOException, URISyntaxException {
        var employment = OBJECT_MAPPER.readValue(validJson, Employment.class);
        employment.setType(new URI(INVALID_URI));
        var gatewayResponse = sendQuery(employment);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.invalidFieldParameterMessage(TYPE)));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidAffiliation() throws IOException, URISyntaxException {
        var employment = OBJECT_MAPPER.readValue(validJson, Employment.class);
        employment.setOrganization(new URI(INVALID_URI));
        var gatewayResponse = sendQuery(employment);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.invalidFieldParameterMessage(ORGANIZATION)));
    }

    @Test
    void shouldBeAllowedToCreateEmploymentAtAllInstitutionsWhenIsAdministrator() throws IOException {
        var dummyEmployment = randomEmployment();
        var gatewayResponse = queryAsAppAdmin(dummyEmployment);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldLogIdentifierOfTheNewlyCreatedResource() throws Exception {
        final var testAppender = LogUtils.getTestingAppender(IdCreatedLogger.class);

        var employment = sampleEmployment();
        var employmentId = "1234";
        var fakeResponse = employment.toCristinEmployment();
        fakeResponse.setId(employmentId);

        when(httpClientMock.<String>send(any(), any()))
            .thenReturn(new HttpResponseFaker(fakeResponse.toString(), 201));
        var apiClient = new CreatePersonEmploymentClient(httpClientMock);
        handler = new CreatePersonEmploymentHandler(apiClient, environment);

        var gatewayResponse = sendQueryWithCristinOrgId(employment);
        var idUri = "https://api.dev.nva.aws.unit.no/cristin/person/112233/employment/1234";

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_CREATED));
        assertThat(testAppender.getMessages(), containsString(String.format(CLIENT_CREATED_RESOURCE_TEMPLATE, idUri)));
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Employment> sendQuery(Employment body) throws IOException {
        var input = createRequest(body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Employment.class);
    }

    private InputStream createRequest(Employment body)
        throws JsonProcessingException {
        var customerId = randomUri();

        return new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
            .withBody(body)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, MANAGE_OWN_AFFILIATION)
            .withPathParameters(validPath)
            .build();
    }

    private GatewayResponse<Employment> queryWithoutRequiredAccessRights() throws IOException {
        var input = new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Employment.class);
    }

    private GatewayResponse<Employment> sendQueryWithCristinOrgId(Employment body) throws IOException {
        var customerId = randomUri();
        var input = new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
                                .withBody(body)
                                .withCurrentCustomer(customerId)
                                .withTopLevelCristinOrgId(TOP_ORG_ID)
                                .withAccessRights(customerId, MANAGE_OWN_AFFILIATION)
                                .withPathParameters(validPath)
                                .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Employment.class);
    }

    private GatewayResponse<Employment> queryAsAppAdmin(Employment body)
        throws IOException {
        var customerId = randomUri();
        var input = new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
                        .withBody(body)
                        .withCurrentCustomer(customerId)
                        .withAccessRights(customerId, MANAGE_CUSTOMERS)
                        .withPathParameters(validPath)
                        .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Employment.class);
    }
}
