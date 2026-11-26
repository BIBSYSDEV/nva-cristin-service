package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.organizationId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.personId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomCustomer;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomInstitutionIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPeriodIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.clinicaltrial.service.SubmissionService.MISSING_INCLUSIONS;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.write.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.model.write.PeriodWrite;
import no.unit.nva.clinicaltrial.model.write.SystemWriter;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.LocalDynamoDbTable;
import no.unit.nva.clinicaltrial.service.SubmissionService;
import no.unit.nva.identifiers.PersonIdentifier;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class SaveSubmissionHandlerTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String ADMIN_PROCESS = "period-administration";
  private static final String IDENTIFIER = "identifier";
  private static final String PERIOD_IDENTIFIER = "periodIdentifier";
  private static final URI INSTITUTION_WITHOUT_IDENTIFIER = URI.create("https://example.org");
  private static final URI WRONG_KIND_OF_IDENTIFIER =
      URI.create("https://api.dev.nva.aws.unit.no/cristin/organization/185.0.0.0");

  private PersonIdentifier projectManagerIdentifier;
  private String projectIdentifier;
  private String periodIdentifier;
  private ClinicalTrialRepository repository;
  private ByteArrayOutputStream output;
  private SaveSubmissionHandler handler;

  @BeforeEach
  void setUp() throws Exception {
    projectManagerIdentifier = randomPersonIdentifier();
    projectIdentifier = randomProjectIdentifier();
    periodIdentifier = randomPeriodIdentifier();
    repository = LocalDynamoDbTable.repositoryOf(LocalDynamoDbTable.create(TABLE_NAME), TABLE_NAME);
    repository.save(
        new PeriodWrite(new Period(periodIdentifier, null), new SystemWriter(ADMIN_PROCESS)));
    repository.save(
        new ClinicalTrialWrite(
            new ClinicalTrial(projectIdentifier, Reportable.YES, null), randomUser()));
    var service =
        new SubmissionService(_ -> projectManagedBy(projectManagerIdentifier), repository);
    output = new ByteArrayOutputStream();
    handler = new SaveSubmissionHandler(service, new Environment());
  }

  @Test
  void shouldReturnCreatedWhenProjectManagerCreatesSubmission() throws Exception {
    handler.handleRequest(postRequest(projectIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Void.class).getStatusCode(), is(HTTP_CREATED));
  }

  @Test
  void shouldReturnConflictWhenSubmissionAlreadyExistsForPeriod() throws Exception {
    handler.handleRequest(postRequest(projectIdentifier), new ByteArrayOutputStream(), context());

    handler.handleRequest(postRequest(projectIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(), is(HTTP_CONFLICT));
  }

  @Test
  void shouldReturnBadRequestNamingRuleWhenInclusionsAreMissing() throws Exception {
    var request = submissionRequest(null);

    handler.handleRequest(request, output, context());

    var problem =
        GatewayResponse.fromOutputStream(output, Problem.class).getBodyObject(Problem.class);
    assertThat(problem.getStatus().getStatusCode(), is(HTTP_BAD_REQUEST));
    assertThat(problem.getDetail(), is(MISSING_INCLUSIONS));
  }

  @Test
  void shouldReturnBadRequestWhenInstitutionUriCarriesNoIdentifier() throws Exception {
    var request =
        submissionRequest(
            new SubmissionRequest(
                List.of(new InclusionRequest(INSTITUTION_WITHOUT_IDENTIFIER, 3))));

    handler.handleRequest(request, output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  @Test
  void shouldReturnBadRequestWhenInclusionIsMissingFromList() throws Exception {
    var request = submissionRequest(new SubmissionRequest(Collections.singletonList(null)));

    handler.handleRequest(request, output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  @Test
  void shouldReturnBadRequestWhenCallerIsNotIdentifiedByPersonUri() throws Exception {
    var request =
        requestBuilder(projectIdentifier).withPersonCristinId(WRONG_KIND_OF_IDENTIFIER).build();

    handler.handleRequest(request, output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  @Test
  void shouldReturnBadRequestWhenCallerCustomerIsNotCustomerUri() throws Exception {
    var request =
        requestBuilder(projectIdentifier).withCurrentCustomer(WRONG_KIND_OF_IDENTIFIER).build();

    handler.handleRequest(request, output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  @Test
  void shouldReturnBadGatewayWhenProjectsApiIsUnavailable() throws Exception {
    var handlerWithUnavailableApi =
        new SaveSubmissionHandler(
            new SubmissionService(
                _ -> {
                  throw new BadGatewayException(randomString());
                },
                repository),
            new Environment());

    handlerWithUnavailableApi.handleRequest(postRequest(projectIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_GATEWAY));
  }

  @Test
  void shouldReturnBadRequestWhenProjectIdentifierIsNotCristinIdentifier() throws Exception {
    handler.handleRequest(postRequest(randomString()), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  private InputStream submissionRequest(SubmissionRequest body) throws Exception {
    return requestBuilder(projectIdentifier).withBody(body).build();
  }

  private InputStream postRequest(String project) throws Exception {
    return requestBuilder(project).build();
  }

  private HandlerRequestBuilder<SubmissionRequest> requestBuilder(String project) throws Exception {
    return new HandlerRequestBuilder<SubmissionRequest>(dtoObjectMapper)
        .withBody(
            new SubmissionRequest(
                List.of(new InclusionRequest(organizationId(randomInstitutionIdentifier()), 3))))
        .withPathParameters(Map.of(IDENTIFIER, project, PERIOD_IDENTIFIER, periodIdentifier))
        .withUserName(randomString())
        .withPersonCristinId(personId(projectManagerIdentifier))
        .withCurrentCustomer(randomCustomer());
  }

  private Context context() {
    return new FakeContext();
  }
}
