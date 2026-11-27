package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.personId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomCustomer;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomInstitutionIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPeriodIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
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

class FetchSubmissionHandlerTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String PROJECT_IDENTIFIER = "identifier";
  private static final String PERIOD_IDENTIFIER = "periodIdentifier";
  private static final String ETAG = "ETag";

  private PersonIdentifier projectManagerIdentifier;
  private String projectIdentifier;
  private String periodIdentifier;
  private ClinicalTrialRepository repository;
  private ByteArrayOutputStream output;
  private FetchSubmissionHandler handler;

  @BeforeEach
  void setUp() {
    projectManagerIdentifier = randomPersonIdentifier();
    projectIdentifier = randomProjectIdentifier();
    periodIdentifier = randomPeriodIdentifier();
    repository = LocalDynamoDbTable.repositoryOf(LocalDynamoDbTable.create(TABLE_NAME), TABLE_NAME);
    var service =
        new SubmissionService(_ -> projectManagedBy(projectManagerIdentifier), repository);
    output = new ByteArrayOutputStream();
    handler = new FetchSubmissionHandler(service, new Environment());
  }

  @Test
  void shouldReturnOkWhenPeriodHasSubmission() throws Exception {
    persistSubmission();

    handler.handleRequest(request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, SubmissionResponse.class).getStatusCode(),
        is(HTTP_OK));
  }

  @Test
  void shouldReturnVersionAsEntityTag() throws Exception {
    persistSubmission();

    handler.handleRequest(request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, SubmissionResponse.class).getHeaders().get(ETAG),
        is("\"1\""));
  }

  @Test
  void shouldReturnNotFoundWhenPeriodHasNoSubmission() throws Exception {
    handler.handleRequest(request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_NOT_FOUND));
  }

  @Test
  void shouldReturnForbiddenWhenCallerIsNotGrantedAccessToProject() throws Exception {
    persistSubmission();

    handler.handleRequest(request(projectIdentifier, randomPersonIdentifier()), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_FORBIDDEN));
  }

  @Test
  void shouldReturnBadGatewayWhenProjectsApiIsUnavailable() throws Exception {
    var handlerWithUnavailableApi =
        new FetchSubmissionHandler(
            new SubmissionService(
                _ -> {
                  throw new BadGatewayException(randomString());
                },
                repository),
            new Environment());

    handlerWithUnavailableApi.handleRequest(
        request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_GATEWAY));
  }

  @Test
  void shouldReturnBadRequestWhenProjectIdentifierIsNotCristinIdentifier() throws Exception {
    handler.handleRequest(request(randomString(), projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  private void persistSubmission() throws Exception {
    repository.save(
        randomUser(),
        new Submission(
            projectIdentifier,
            periodIdentifier,
            List.of(new Inclusion(randomInstitutionIdentifier(), 9)),
            SubmissionStatus.SUBMITTED,
            null));
  }

  private InputStream request(String project, PersonIdentifier personIdentifier) throws Exception {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withPathParameters(
            Map.of(PROJECT_IDENTIFIER, project, PERIOD_IDENTIFIER, periodIdentifier))
        .withUserName(randomString())
        .withPersonCristinId(personId(personIdentifier))
        .withCurrentCustomer(randomCustomer())
        .build();
  }

  private Context context() {
    return new FakeContext();
  }
}
