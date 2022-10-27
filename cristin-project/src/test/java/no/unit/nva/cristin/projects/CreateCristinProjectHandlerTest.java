package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.SOME_UNIT_IDENTIFIER;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomContributorWithUnitAffiliation;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomContributorWithoutUnitAffiliation;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomMinimalNvaProject;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.someOrganizationFromUnitIdentifier;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import no.unit.nva.cristin.projects.model.cristin.CristinDateInfo;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.DateInfo;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateCristinProjectHandlerTest {

    public static final String NO_ACCESS = "NoAccess";
    public static final String ILLEGAL_CONTRIBUTOR_ROLE = "illegalContributorRole";
    public static final int FIRST_CONTRIBUTOR = 0;

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
        GatewayResponse<Object> response = GatewayResponse.fromOutputStream(output, Object.class);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_FORBIDDEN));
    }

    @Test
    void shouldReturn400BadRequestWhenMissingRequiredFieldsInInput() throws Exception {

        NvaProject randomNvaProject = randomNvaProject();
        randomNvaProject.setId(randomUri());
        randomNvaProject.setTitle(null);

        GatewayResponse<NvaProject> response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @Test
    void shouldReturn400BadRequestWhenIllegalRoleValueInInput() throws Exception {

        NvaProject randomNvaProject = randomNvaProject();
        randomNvaProject.getContributors().get(FIRST_CONTRIBUTOR).setType(ILLEGAL_CONTRIBUTOR_ROLE);

        GatewayResponse<NvaProject> response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @Test
    void shouldReturnProjectDataWithNewIdentifierWhenCreated() throws Exception {
        NvaProject expected = randomNvaProject();
        expected.setContext(NvaProject.PROJECT_CONTEXT);
        mockUpstreamUsingRequest(expected);

        var identifier = expected.getId(); // We need to put this back after request
        expected.setId(null); // Cannot create with Id
        GatewayResponse<NvaProject> response = executeRequest(expected);
        expected.setId(identifier);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));
        NvaProject actual = response.getBodyObject(NvaProject.class);
        assertNotNull(actual.getId());
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnMinimalProjectDataWhenCreatedWithTitleAndStatus() throws Exception {
        NvaProject expected = randomMinimalNvaProject();
        expected.setContext(NvaProject.PROJECT_CONTEXT);
        mockUpstreamUsingRequest(expected);

        NvaProject requestProject = expected.toCristinProject().toNvaProject();
        requestProject.setId(null);  // Cannot create with Id
        GatewayResponse<NvaProject> response = executeRequest(requestProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));
        NvaProject actual = response.getBodyObject(NvaProject.class);
        assertNotNull(actual.getId());
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnProjectDataWhenCreatingWithUnitIdentifier() throws IOException, InterruptedException {
        NvaProject request = nvaProjectUsingUnitIdentifiers();
        mockUpstreamUsingRequest(request);
        GatewayResponse<NvaProject> response = executeRequest(request);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));

        NvaProject actual = response.getBodyObject(NvaProject.class);

        Organization actualOrganization = actual.getCoordinatingInstitution();
        Organization expectedOrganization = request.getCoordinatingInstitution();
        assertThat(actualOrganization, equalTo(expectedOrganization));
        assertThat(actualIdentifierFromOrganization(actualOrganization), equalTo(SOME_UNIT_IDENTIFIER));

        Organization actualAffiliation = actual.getContributors().get(0).getAffiliation();
        Organization expectedAffiliation = request.getContributors().get(0).getAffiliation();
        assertThat(actualAffiliation, equalTo(expectedAffiliation));
        assertThat(actualIdentifierFromOrganization(actualAffiliation), equalTo(SOME_UNIT_IDENTIFIER));
    }

    @Test
    void shouldCreateProjectWithoutNonRequiredFieldContributorAffiliation()
        throws IOException, InterruptedException {

        NvaProject request = nvaProjectWithoutContributorAffiliation();
        mockUpstreamUsingRequest(request);
        GatewayResponse<NvaProject> response = executeRequest(request);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_CREATED));

        NvaProject actual = response.getBodyObject(NvaProject.class);

        Organization actualAffiliation = actual.getContributors().get(0).getAffiliation();
        Organization expectedAffiliation = request.getContributors().get(0).getAffiliation();
        assertThat(actualAffiliation, equalTo(expectedAffiliation));
    }

    private String actualIdentifierFromOrganization(Organization organization) {
        return extractLastPathElement(organization.getId());
    }

    private void mockUpstreamUsingRequest(NvaProject request) throws IOException, InterruptedException {
        var cristinProject = request.toCristinProject();
        addMockedResponseFieldsToCristinProject(cristinProject, request);
        HttpResponse<String> httpResponse =
            new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinProject), 201);
        when(mockHttpClient.send(any(), any())).thenAnswer(response -> httpResponse);
    }

    private void addMockedResponseFieldsToCristinProject(CristinProject cristinProject, NvaProject request) {
        cristinProject.setCreated(fromDateInfo(request.getCreated()));
        cristinProject.setLastModified(fromDateInfo(request.getLastModified()));
    }

    private CristinDateInfo fromDateInfo(DateInfo dateInfo) {
        if (Objects.isNull(dateInfo)) {
            return null;
        }
        var cristinDateInfo = new CristinDateInfo();
        cristinDateInfo.setDate(dateInfo.getDate());
        cristinDateInfo.setSourceShortName(dateInfo.getSourceShortName());
        return cristinDateInfo;
    }

    private GatewayResponse<NvaProject> executeRequest(NvaProject request) throws IOException {
        InputStream input = requestWithBodyAndRole(request);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, NvaProject.class);
    }

    private NvaProject nvaProjectUsingUnitIdentifiers() {
        NvaProject requestBody = randomMinimalNvaProject();
        requestBody.setId(null);
        requestBody.setCoordinatingInstitution(someOrganizationFromUnitIdentifier());
        requestBody.setContributors(List.of(randomContributorWithUnitAffiliation()));
        return requestBody;
    }

    private NvaProject nvaProjectWithoutContributorAffiliation() {
        NvaProject requestBody = randomMinimalNvaProject();
        requestBody.setId(null);
        requestBody.setCoordinatingInstitution(someOrganizationFromUnitIdentifier());
        requestBody.setContributors(List.of(randomContributorWithoutUnitAffiliation()));
        return requestBody;
    }

    private InputStream requestWithBodyAndRole(NvaProject body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<NvaProject>(OBJECT_MAPPER)
            .withBody(body)
            .withCustomerId(customerId)
            .withAccessRights(customerId, EDIT_OWN_INSTITUTION_PROJECTS)
            .build();
    }
}
