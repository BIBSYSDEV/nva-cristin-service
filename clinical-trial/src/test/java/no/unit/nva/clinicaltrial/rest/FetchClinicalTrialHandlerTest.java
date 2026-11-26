package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.personId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomCustomer;
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
import java.util.Map;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.LocalDynamoDbTable;
import no.unit.nva.clinicaltrial.service.ClinicalTrialService;
import no.unit.nva.identifiers.PersonIdentifier;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

class FetchClinicalTrialHandlerTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String PROJECT_IDENTIFIER = "identifier";
  private static final String ETAG = "ETag";

  private PersonIdentifier projectManagerIdentifier;
  private String projectIdentifier;
  private ClinicalTrialRepository repository;
  private ByteArrayOutputStream output;
  private FetchClinicalTrialHandler handler;

  @BeforeEach
  void setUp() {
    projectManagerIdentifier = randomPersonIdentifier();
    projectIdentifier = randomProjectIdentifier();
    repository = LocalDynamoDbTable.repositoryOf(LocalDynamoDbTable.create(TABLE_NAME), TABLE_NAME);
    var service =
        new ClinicalTrialService(_ -> projectManagedBy(projectManagerIdentifier), repository);
    output = new ByteArrayOutputStream();
    handler = new FetchClinicalTrialHandler(service, new Environment());
  }

  @Test
  void shouldReturnOkWhenProjectHasClinicalTrial() throws Exception {
    persistClinicalTrial();

    handler.handleRequest(request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, ClinicalTrialResponse.class).getStatusCode(),
        is(HTTP_OK));
  }

  @Test
  void shouldReturnVersionAsEntityTag() throws Exception {
    persistClinicalTrial();

    handler.handleRequest(request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, ClinicalTrialResponse.class)
            .getHeaders()
            .get(ETAG),
        is("\"1\""));
  }

  @Test
  void shouldReturnNotFoundWhenProjectHasNoClinicalTrial() throws Exception {
    handler.handleRequest(request(projectIdentifier, projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_NOT_FOUND));
  }

  @Test
  void shouldReturnForbiddenWhenCallerIsNotGrantedAccessToProject() throws Exception {
    persistClinicalTrial();

    handler.handleRequest(request(projectIdentifier, randomPersonIdentifier()), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_FORBIDDEN));
  }

  @Test
  void shouldReturnBadRequestWhenProjectIdentifierIsNotCristinIdentifier() throws Exception {
    handler.handleRequest(request(randomString(), projectManagerIdentifier), output, context());

    assertThat(
        GatewayResponse.fromOutputStream(output, Problem.class).getStatusCode(),
        is(HTTP_BAD_REQUEST));
  }

  private void persistClinicalTrial() throws Exception {
    repository.save(randomUser(), new ClinicalTrial(projectIdentifier, Reportable.YES, null));
  }

  private InputStream request(String project, PersonIdentifier personIdentifier) throws Exception {
    return new HandlerRequestBuilder<Void>(dtoObjectMapper)
        .withPathParameters(Map.of(PROJECT_IDENTIFIER, project))
        .withUserName(randomString())
        .withPersonCristinId(personId(personIdentifier))
        .withCurrentCustomer(randomCustomer())
        .build();
  }

  private Context context() {
    return new FakeContext();
  }
}
