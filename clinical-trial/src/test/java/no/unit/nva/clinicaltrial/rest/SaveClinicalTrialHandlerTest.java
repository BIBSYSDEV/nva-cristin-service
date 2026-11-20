package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PRECON_FAILED;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.personId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomCustomer;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.LocalDynamoDbTable;
import no.unit.nva.clinicaltrial.service.ClinicalTrialService;
import no.unit.nva.cristin.projects.model.nva.Approval;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.HealthProjectType;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.identifiers.SortableIdentifier;
import no.unit.nva.model.ApprovalStatus;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class SaveClinicalTrialHandlerTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String PROJECT_MANAGER = "ProjectManager";
  private static final String IDENTIFIER = "identifier";
  private static final String IF_MATCH = "If-Match";
  private static final String ETAG = "ETag";
  private static final String OPERATION_NAME = "operationName";
  private static final String CREATE_OPERATION_ID = "CreateClinicalTrial";
  private static final String UPDATE_OPERATION_ID = "UpdateClinicalTrial";

  private String projectManagerIdentifier;
  private ByteArrayOutputStream output;
  private SaveClinicalTrialHandler handler;

  @BeforeEach
  void setUp() {
    projectManagerIdentifier = randomPersonIdentifier();
    var repository = new ClinicalTrialRepository(LocalDynamoDbTable.create(TABLE_NAME), TABLE_NAME);
    var service = new ClinicalTrialService(_ -> projectManagedBy(), repository);
    output = new ByteArrayOutputStream();
    handler = new SaveClinicalTrialHandler(service, new Environment());
  }

  @Test
  void shouldReturnCreatedWithEtagWhenProjectManagerCreatesClinicalTrial() throws Exception {
    handler.handleRequest(postRequest(randomProjectIdentifier()), output, context());

    var response = GatewayResponse.fromOutputStream(output, ClinicalTrialResponse.class);
    assertThat(response.getStatusCode(), is(HTTP_CREATED));
    assertThat(response.getHeaders().get(ETAG), is(notNullValue()));
  }

  @Test
  void shouldReturnConflictWhenCreatingClinicalTrialForProjectThatAlreadyHasOne() throws Exception {
    var projectIdentifier = randomProjectIdentifier();
    handler.handleRequest(postRequest(projectIdentifier), new ByteArrayOutputStream(), context());

    handler.handleRequest(postRequest(projectIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(), is(HTTP_CONFLICT));
  }

  @Test
  void shouldReturnOkWhenProjectManagerUpdatesClinicalTrialWithCurrentVersion() throws Exception {
    var projectIdentifier = randomProjectIdentifier();
    var created = new ByteArrayOutputStream();
    handler.handleRequest(postRequest(projectIdentifier), created, context());
    var version =
        GatewayResponse.fromOutputStream(created, ClinicalTrialResponse.class)
            .getHeaders()
            .get(ETAG);

    handler.handleRequest(putRequest(projectIdentifier, version), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, ClinicalTrialResponse.class).getStatusCode(),
        is(HTTP_OK));
  }

  @Test
  void shouldReturnNotFoundWhenUpdatingClinicalTrialForProjectWithoutOne() throws Exception {
    handler.handleRequest(
        putRequest(randomProjectIdentifier(), SortableIdentifier.next().toString()),
        output,
        context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_NOT_FOUND));
  }

  @Test
  void shouldReturnPreconditionRequiredWhenUpdatingWithoutIfMatchHeader() throws Exception {
    var projectIdentifier = randomProjectIdentifier();
    handler.handleRequest(postRequest(projectIdentifier), new ByteArrayOutputStream(), context());

    handler.handleRequest(putRequest(projectIdentifier, null), output, context());

    assertThat(GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(), is(428));
  }

  @Test
  void shouldReturnPreconditionFailedWhenUpdatingWithOutdatedVersion() throws Exception {
    var projectIdentifier = randomProjectIdentifier();
    handler.handleRequest(postRequest(projectIdentifier), new ByteArrayOutputStream(), context());

    handler.handleRequest(
        putRequest(projectIdentifier, SortableIdentifier.next().toString()), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_PRECON_FAILED));
  }

  @Test
  void shouldReturnBadRequestWhenProjectIdentifierIsNotCristinIdentifier() throws Exception {
    handler.handleRequest(postRequest(randomString()), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  @Test
  void shouldReturnForbiddenWhenCallerIsNotProjectManager() throws Exception {
    var request =
        creating(requestBuilder(randomProjectIdentifier(), randomPersonIdentifier())).build();

    handler.handleRequest(request, output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_FORBIDDEN));
  }

  private InputStream postRequest(String projectIdentifier) throws Exception {
    return creating(requestBuilder(projectIdentifier, projectManagerIdentifier)).build();
  }

  private InputStream putRequest(String projectIdentifier, String version) throws Exception {
    var builder = updating(requestBuilder(projectIdentifier, projectManagerIdentifier));
    return version == null
        ? builder.build()
        : builder.withHeaders(Map.of(IF_MATCH, version)).build();
  }

  private HandlerRequestBuilder<Void> creating(HandlerRequestBuilder<Void> builder) {
    return builder.withRequestContextValue(OPERATION_NAME, CREATE_OPERATION_ID);
  }

  private HandlerRequestBuilder<Void> updating(HandlerRequestBuilder<Void> builder) {
    return builder.withRequestContextValue(OPERATION_NAME, UPDATE_OPERATION_ID);
  }

  private HandlerRequestBuilder<Void> requestBuilder(
      String projectIdentifier, String personIdentifier) {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withPathParameters(Map.of(IDENTIFIER, projectIdentifier))
        .withUserName(randomString())
        .withPersonCristinId(personId(personIdentifier))
        .withCurrentCustomer(randomCustomer());
  }

  private NvaProject projectManagedBy() {
    return new NvaProject.Builder()
        .withContributors(
            List.of(
                new NvaContributor(
                    person(projectManagerIdentifier), List.of(new Role(PROJECT_MANAGER, null)))))
        .withHealthProjectData(new HealthProjectData(HealthProjectType.DRUGSTUDY, null, null))
        .withApprovals(List.of(new Approval(null, null, ApprovalStatus.APPROVED, null, null, null)))
        .build();
  }

  private Person person(String personIdentifier) {
    return new Person(
        personId(personIdentifier), randomString(), randomString(), randomString(), randomString());
  }

  private Context context() {
    return new FakeContext();
  }
}
