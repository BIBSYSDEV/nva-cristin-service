package no.unit.nva.cristin.projects.create;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.SOME_UNIT_IDENTIFIER;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomContributorWithUnitAffiliation;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomContributorWithoutUnitAffiliation;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomMinimalNvaProject;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNamesMap;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.someOrganizationFromUnitIdentifier;
import static no.unit.nva.cristin.projects.create.CreateCristinProjectValidator.INVALID_CLINICAL_TRIAL_PHASE;
import static no.unit.nva.cristin.projects.create.CreateCristinProjectValidator.INVALID_HEALTH_PROJECT_TYPE;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.List;
import no.unit.nva.cristin.projects.create.CreateCristinProjectValidator.ValidatedResult;
import no.unit.nva.cristin.projects.model.cristin.CristinDateInfo;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.DateInfo;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateCristinProjectHandlerTest {

    public static final String NO_ACCESS = "NoAccess";
    public static final String ILLEGAL_CONTRIBUTOR_ROLE = "illegalContributorRole";
    public static final int FIRST_CONTRIBUTOR = 0;
    public static final String DRUGSTUDY = "DRUGSTUDY";
    public static final String INVALIDVALUE = "INVALIDVALUE";
    public static final String CLINICAL_TRIAL_PHASE_VALUE = "1";
    public static final String OTHERSTUDY = "OTHERSTUDY";

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
        var randomNvaProject = randomNvaProject();
        var input = new HandlerRequestBuilder<NvaProject>(OBJECT_MAPPER)
            .withBody(randomNvaProject)
            .withRoles(NO_ACCESS)
            .build();
        handler.handleRequest(input, output, context);
        var response = GatewayResponse.fromOutputStream(output, Object.class);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_FORBIDDEN));
    }

    @Test
    void shouldReturn400BadRequestWhenIdIsDefined() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(randomUri());

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(ValidatedResult.HasId.label));
    }

    @Test
    void shouldReturn400BadRequestWhenNoTitle() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        randomNvaProject.setTitle(null);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(ValidatedResult.NoTitle.label));
    }

    @Test
    void shouldReturn400BadRequestWhenInvalidStartDate() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        randomNvaProject.setStartDate(null);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(ValidatedResult.InvalidStartDate.label));
    }

    @Test
    void shouldReturn400BadRequestWhenHasNoContributors() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        randomNvaProject.setContributors(null);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(ValidatedResult.HasNoContributors.label));
    }

    @Test
    void shouldReturn400BadRequestWhenHasNoCoordinatingOrganization() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        randomNvaProject.setCoordinatingInstitution(null);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(ValidatedResult.HasNoCoordinatingOrganization.label));
    }

    @Test
    void shouldReturn400BadRequestWhenIllegalRoleValueInInput() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.getContributors().get(FIRST_CONTRIBUTOR).setType(ILLEGAL_CONTRIBUTOR_ROLE);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
    }

    @Test
    void shouldReturnProjectDataWithNewIdentifierWhenCreated() throws Exception {
        var expected = randomNvaProject();
        expected.setContext(NvaProject.PROJECT_CONTEXT);
        mockUpstreamUsingRequest(expected);

        var identifier = expected.getId(); // We need to put this back after request
        expected.setId(null); // Cannot create with Id
        var response = executeRequest(expected);
        expected.setId(identifier);
        removeFieldsNotSupportedByPost(expected);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));
        var actual = response.getBodyObject(NvaProject.class);
        assertNotNull(actual.getId());
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnMinimalProjectDataWhenCreatedWithTitleAndStatus() throws Exception {
        var expected = randomMinimalNvaProject();
        expected.setContext(NvaProject.PROJECT_CONTEXT);
        mockUpstreamUsingRequest(expected);

        var requestProject = expected.toCristinProject().toNvaProject();
        requestProject.setId(null);  // Cannot create with Id
        var response = executeRequest(requestProject);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));
        var actual = response.getBodyObject(NvaProject.class);
        assertNotNull(actual.getId());
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnProjectDataWhenCreatingWithUnitIdentifier() throws IOException, InterruptedException {
        var request = nvaProjectUsingUnitIdentifiers();
        mockUpstreamUsingRequest(request);
        var response = executeRequest(request);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));

        var actual = response.getBodyObject(NvaProject.class);

        var actualOrganization = actual.getCoordinatingInstitution();
        var expectedOrganization = request.getCoordinatingInstitution();
        assertThat(actualOrganization, equalTo(expectedOrganization));
        assertThat(actualIdentifierFromOrganization(actualOrganization), equalTo(SOME_UNIT_IDENTIFIER));

        var actualAffiliation = actual.getContributors().get(0).getAffiliation();
        var expectedAffiliation = request.getContributors().get(0).getAffiliation();
        assertThat(actualAffiliation, equalTo(expectedAffiliation));
        assertThat(actualIdentifierFromOrganization(actualAffiliation), equalTo(SOME_UNIT_IDENTIFIER));
    }

    @Test
    void shouldCreateProjectWithoutNonRequiredFieldContributorAffiliation() throws IOException, InterruptedException {
        var request = nvaProjectWithoutContributorAffiliation();
        mockUpstreamUsingRequest(request);
        var response = executeRequest(request);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));

        var actual = response.getBodyObject(NvaProject.class);

        var actualAffiliation = actual.getContributors().get(0).getAffiliation();
        var expectedAffiliation = request.getContributors().get(0).getAffiliation();
        assertThat(actualAffiliation, equalTo(expectedAffiliation));
    }

    @Test
    void shouldAllowCreationOfProjectWithoutLanguage() throws IOException, InterruptedException {
        var request = randomMinimalNvaProject();
        request.setId(null); // Is not supported for input
        request.setLanguage(null);
        mockUpstreamUsingRequest(request);
        var response = executeRequest(request);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));
    }

    @Test
    void shouldReturnBadRequestWhenHealthProjectDataTypePresentButInvalid() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        var healthProjectData = new HealthProjectData(INVALIDVALUE, randomNamesMap(), CLINICAL_TRIAL_PHASE_VALUE);
        randomNvaProject.setHealthProjectData(healthProjectData);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(INVALID_HEALTH_PROJECT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenHealthProjectDataClinicalPhasePresentButInvalid() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        var healthProjectData = new HealthProjectData(DRUGSTUDY, randomNamesMap(), INVALIDVALUE);
        randomNvaProject.setHealthProjectData(healthProjectData);

        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_REQUEST));
        assertThat(response.getBody(), containsString(INVALID_CLINICAL_TRIAL_PHASE));
    }

    @Test
    void shouldReturnCreatedWhenHealthProjectDataHasPartialData() throws Exception {
        var randomNvaProject = randomNvaProject();
        randomNvaProject.setId(null);
        var healthProjectData = new HealthProjectData(OTHERSTUDY, null, null);
        randomNvaProject.setHealthProjectData(healthProjectData);

        mockUpstreamUsingRequest(randomNvaProject);
        var response = executeRequest(randomNvaProject);

        assertThat(response.getStatusCode(), equalTo(HTTP_CREATED));
    }

    @Test
    void shouldHaveProjectWithHealthDataAddedAndSentToUpstream() throws Exception {
        var apiClient = new CreateCristinProjectApiClient(mockHttpClient);
        apiClient = spy(apiClient);
        handler = new CreateCristinProjectHandler(apiClient, environment);

        var nvaProject = randomNvaProject();
        nvaProject.setId(null);
        var healthProjectData = new HealthProjectData(DRUGSTUDY, null, CLINICAL_TRIAL_PHASE_VALUE);
        nvaProject.setHealthProjectData(healthProjectData);

        mockUpstreamUsingRequest(nvaProject);
        executeRequest(nvaProject);

        var captor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).post(any(), captor.capture());
        var capturedCristinProject = OBJECT_MAPPER.readValue(captor.getValue(), CristinProject.class);

        assertThat(capturedCristinProject.getHealthProjectType(),
                   equalTo(nvaProject.getHealthProjectData().getType()));
        assertThat(capturedCristinProject.getClinicalTrialPhase(),
                   equalTo(nvaProject.getHealthProjectData().getClinicalTrialPhase()));
    }

    private String actualIdentifierFromOrganization(Organization organization) {
        return extractLastPathElement(organization.getId());
    }

    private void mockUpstreamUsingRequest(NvaProject request) throws IOException, InterruptedException {
        var cristinProject = request.toCristinProject();
        addMockedResponseFieldsToCristinProject(cristinProject, request);
        var httpResponse = new HttpResponseFaker(OBJECT_MAPPER.writeValueAsString(cristinProject), 201);
        when(mockHttpClient.send(any(), any())).thenAnswer(response -> httpResponse);
    }

    private void addMockedResponseFieldsToCristinProject(CristinProject cristinProject, NvaProject request) {
        cristinProject.setCreated(fromDateInfo(request.getCreated()));
        cristinProject.setLastModified(fromDateInfo(request.getLastModified()));
    }

    private CristinDateInfo fromDateInfo(DateInfo dateInfo) {
        if (isNull(dateInfo)) {
            return null;
        }
        return new CristinDateInfo(dateInfo.getSourceShortName(), dateInfo.getDate());
    }

    private GatewayResponse<NvaProject> executeRequest(NvaProject request) throws IOException {
        var input = requestWithBodyAndRole(request);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, NvaProject.class);
    }

    private NvaProject nvaProjectUsingUnitIdentifiers() {
        var requestBody = randomMinimalNvaProject();
        requestBody.setId(null);
        requestBody.setCoordinatingInstitution(someOrganizationFromUnitIdentifier());
        requestBody.setContributors(List.of(randomContributorWithUnitAffiliation()));
        return requestBody;
    }

    private NvaProject nvaProjectWithoutContributorAffiliation() {
        var requestBody = randomMinimalNvaProject();
        requestBody.setId(null);
        requestBody.setCoordinatingInstitution(someOrganizationFromUnitIdentifier());
        requestBody.setContributors(List.of(randomContributorWithoutUnitAffiliation()));
        return requestBody;
    }

    private InputStream requestWithBodyAndRole(NvaProject body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<NvaProject>(OBJECT_MAPPER)
            .withBody(body)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, EDIT_OWN_INSTITUTION_PROJECTS)
            .build();
    }

    //TODO: Remove each of these when supported by POST
    private void removeFieldsNotSupportedByPost(NvaProject expected) {
        expected.setContactInfo(null);
        expected.setFundingAmount(null);
        expected.setProjectCategories(Collections.emptyList());
        expected.setKeywords(Collections.emptyList());
        expected.setExternalSources(Collections.emptyList());
        expected.setRelatedProjects(Collections.emptyList());
    }

}
