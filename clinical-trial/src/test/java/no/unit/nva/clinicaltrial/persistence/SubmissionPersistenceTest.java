package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.submission;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import no.unit.nva.clinicaltrial.model.write.SubmissionWrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

class SubmissionPersistenceTest {

  private static final String TABLE_NAME = "clinical-trial";

  private DynamoDbClient client;
  private ClinicalTrialRepository repository;

  @BeforeEach
  void setUp() {
    client = LocalDynamoDbTable.create(TABLE_NAME);
    repository = LocalDynamoDbTable.repositoryOf(client, TABLE_NAME);
  }

  @Test
  void shouldFindPersistedSubmission() throws Exception {
    var submission = submission(null);

    repository.save(new SubmissionWrite(submission, randomUser()));

    var persisted = persistedSubmission(submission);
    assertThat(persisted.inclusions(), is(submission.inclusions()));
    assertThat(persisted.status(), is(SubmissionStatus.SUBMITTED));
  }

  @Test
  void shouldPersistSubmissionWithExpectedAttributeNames() throws Exception {
    var submission = submission(null);

    repository.save(new SubmissionWrite(submission, randomUser()));

    var item = persistedItem(submission);
    assertThat(
        item.keySet(),
        is(
            Set.of(
                "PK",
                "SK",
                "type",
                "inclusions",
                "status",
                "version",
                "createdDate",
                "modifiedDate")));
    assertThat(
        item.get("inclusions").l().getFirst().m().keySet(),
        is(Set.of("institutionIdentifier", "count")));
  }

  @Test
  void shouldListVersionsOfSubmissionNewestFirst() throws Exception {
    var submission = submission(null);
    repository.save(new SubmissionWrite(submission, randomUser()));

    repository.save(new SubmissionWrite(persistedSubmission(submission), randomUser()));

    var versions =
        repository.findSubmissionVersions(
            submission.projectIdentifier(), submission.periodIdentifier());
    assertThat(versions.getFirst().entity().version(), is(2L));
    assertThat(versions.getLast().action(), is(Action.CREATE_SUBMISSION));
  }

  private Submission persistedSubmission(Submission submission) {
    return repository
        .findSubmission(submission.projectIdentifier(), submission.periodIdentifier())
        .orElseThrow();
  }

  private Map<String, AttributeValue> persistedItem(Submission submission) {
    return client
        .getItem(
            GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(
                    Map.of(
                        "PK",
                        AttributeValue.fromS("PROJECT#" + submission.projectIdentifier()),
                        "SK",
                        AttributeValue.fromS("SUBMISSION#" + submission.periodIdentifier())))
                .build())
        .item();
  }
}
