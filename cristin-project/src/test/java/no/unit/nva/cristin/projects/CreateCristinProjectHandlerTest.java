package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomMinimalNvaProject;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateCristinProjectHandlerTest {

    public static final String NO_ACCESS = "NoAccess";
    public static final String ILLEGAL_CONTRIBUTOR_ROLE = "illegalContributorRole";
    public static final int FIRST_CONTRIBUTOR = 0;
    public static final String SOME_UNIT_IDENTIFIER = "185.90.0.0";

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private HttpClient mockHttpClient;
    private CreateCristinProjectHandler handler;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        mockHttpClient = mock(HttpClient.class);
        CreateCristinProjectApiClient apiClient = new CreateCristinProjectApiClient(mockHttpClient);
        handler = new CreateCristinProjectHandler(apiClient, environment);
    }

    @Test
    void shouldReturn403ForbiddenWhenRequestIsMissingRole() throws Exception {
        NvaProject randomNvaProject = randomNvaProject();
        InputStream input = new HandlerRequestBuilder<NvaProject>(OBJECT_MAPPER)
                .withBody(randomNvaProject)
                .withRoles(NO_ACCESS)
                .build();
        handler.handleRequest(input, output, context);
        GatewayResponse<Object> response = GatewayResponse.fromOutputStream(output);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_FORBIDDEN));
    }

    @Test
    void shouldReturn400BadRequestWhenMissingRequiredFieldsInInput() throws Exception {

        NvaProject randomNvaProject = randomNvaProject();
        randomNvaProject.setId(randomUri());
        randomNvaProject.setTitle(null);

        InputStream input = requestWithBodyAndRole(randomNvaProject);
        handler.handleRequest(input, output, context);
        GatewayResponse<NvaProject> response = GatewayResponse.fromOutputStream(output);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @Test
    void shouldReturn400BadRequestWhenIllegalRoleValueInInput() throws Exception {

        NvaProject randomNvaProject = randomNvaProject();
        randomNvaProject.getContributors().get(FIRST_CONTRIBUTOR).setType(ILLEGAL_CONTRIBUTOR_ROLE);

        InputStream input = requestWithBodyAndRole(randomNvaProject);
        handler.handleRequest(input, output, context);
        GatewayResponse<NvaProject> response = GatewayResponse.fromOutputStream(output);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @Test
    void shouldReturnProjectDataWithNewIdentifierWhenCreated() throws Exception {
        NvaProject expected = randomNvaProject();
        expected.setContext(NvaProject.PROJECT_CONTEXT);
        HttpResponse<String> httpResponse =
                new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(expected.toCristinProject()), 201);
        when(mockHttpClient.send(any(), any())).thenAnswer(response -> httpResponse);

        NvaProject requestProject = expected.toCristinProject().toNvaProject();
        requestProject.setId(null);  // Cannot create with Id
        InputStream input = requestWithBodyAndRole(requestProject);
        handler.handleRequest(input, output, context);
        GatewayResponse<NvaProject> response = GatewayResponse.fromOutputStream(output);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));
        NvaProject actual = response.getBodyObject(NvaProject.class);
        assertNotNull(actual.getId());
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnMinimalProjectDataWhenCreatedWithTitleAndStatus() throws Exception {
        NvaProject expected = randomMinimalNvaProject();
        expected.setContext(NvaProject.PROJECT_CONTEXT);
        HttpResponse<String> httpResponse =
                new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(expected.toCristinProject()), 201);
        when(mockHttpClient.send(any(), any())).thenAnswer(response -> httpResponse);

        NvaProject requestProject = expected.toCristinProject().toNvaProject();
        requestProject.setId(null);  // Cannot create with Id
        InputStream input = requestWithBodyAndRole(requestProject);
        handler.handleRequest(input, output, context);
        GatewayResponse<NvaProject> response = GatewayResponse.fromOutputStream(output);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));
        NvaProject actual = response.getBodyObject(NvaProject.class);
        assertNotNull(actual.getId());
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnProjectDataWhenCreatingWithUnitIdentifier() throws IOException, InterruptedException {
        NvaProject requestBody = randomMinimalNvaProject();
        requestBody.setId(null);
        requestBody.setCoordinatingInstitution(someOrganizationFromUnitIdentifier());

        HttpResponse<String> httpResponse =
            new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(requestBody.toCristinProject()), 201);
        when(mockHttpClient.send(any(), any())).thenAnswer(response -> httpResponse);

        InputStream input = requestWithBodyAndRole(requestBody);
        handler.handleRequest(input, output, context);
        GatewayResponse<NvaProject> response = GatewayResponse.fromOutputStream(output);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));
        NvaProject actual = response.getBodyObject(NvaProject.class);

        Organization actualOrganization = actual.getCoordinatingInstitution();
        Organization requestOrganization = requestBody.getCoordinatingInstitution();

        assertThat(actualOrganization, equalTo(requestOrganization));
        assertThat(extractLastPathElement(actualOrganization.getId()), equalTo(SOME_UNIT_IDENTIFIER));
    }

    private static Organization someOrganizationFromUnitIdentifier() {
        return new Organization.Builder().withId(getNvaApiId(SOME_UNIT_IDENTIFIER, ORGANIZATION_PATH)).build();
    }

    private InputStream requestWithBodyAndRole(NvaProject body) throws JsonProcessingException {
        return new HandlerRequestBuilder<NvaProject>(OBJECT_MAPPER)
            .withBody(body)
            .withAccessRight(EDIT_OWN_INSTITUTION_PROJECTS)
            .build();
    }
}
