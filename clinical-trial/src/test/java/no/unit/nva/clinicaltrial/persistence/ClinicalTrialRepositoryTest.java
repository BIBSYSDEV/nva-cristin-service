package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrialAdmin;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPeriodIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

class ClinicalTrialRepositoryTest {

  private static final String TABLE_NAME = "clinical-trial";

  private DynamoDbClient client;
  private ClinicalTrialRepository repository;

  @BeforeEach
  void setUp() {
    client = LocalDynamoDbTable.create(TABLE_NAME);
    repository = LocalDynamoDbTable.repositoryOf(client, TABLE_NAME);
  }

  @Test
  void shouldSaveClinicalTrialWithFirstVersion() throws Exception {
    var clinicalTrial = clinicalTrial(null);

    repository.save(randomUser(), clinicalTrial);

    assertThat(persisted(clinicalTrial).version(), is(1L));
  }

  @Test
  void shouldReturnOptionalEmptyWhenProjectHasNoClinicalTrial() {
    assertThat(repository.findClinicalTrial(randomProjectIdentifier()), is(Optional.empty()));
  }

  @Test
  void shouldPersistClinicalTrialWithExpectedAttributeNames() throws Exception {
    var clinicalTrial = clinicalTrial(null);

    repository.save(randomUser(), clinicalTrial);

    assertThat(
        persistedItem(clinicalTrial.projectIdentifier()).keySet(),
        is(Set.of("PK", "SK", "type", "reportable", "version", "modifiedDate")));
  }

  @Test
  void shouldRaiseVersionWhenClinicalTrialIsUpdated() throws Exception {
    var clinicalTrial = clinicalTrial(Reportable.YES, null);
    repository.save(randomUser(), clinicalTrial);

    repository.save(randomUser(), persisted(clinicalTrial));

    assertThat(persistedItem(clinicalTrial.projectIdentifier()).get("version").n(), is("2"));
  }

  @Test
  void shouldPersistChangedReportableWhenClinicalTrialIsUpdated() throws Exception {
    var clinicalTrial = clinicalTrial(Reportable.YES, null);
    repository.save(randomUser(), clinicalTrial);
    var persisted = persisted(clinicalTrial);

    repository.save(
        randomUser(),
        new ClinicalTrial(persisted.projectIdentifier(), Reportable.NO, persisted.version()));

    assertThat(persistedItem(clinicalTrial.projectIdentifier()).get("reportable").s(), is("No"));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenCreatingClinicalTrialForProjectThatAlreadyHasOne()
      throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);

    assertThrows(
        ConditionFailedException.class, () -> repository.save(randomUser(), clinicalTrial));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenUpdatingClinicalTrialWithOutdatedVersion()
      throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);
    var outdated = new ClinicalTrial(clinicalTrial.projectIdentifier(), Reportable.NO, 7L);

    assertThrows(ConditionFailedException.class, () -> repository.save(randomUser(), outdated));
  }

  @Test
  void shouldThrowItemNotFoundExceptionWhenUpdatingNonExistingClinicalTrial() {
    var clinicalTrial = clinicalTrial(Reportable.YES, 1L);

    assertThrows(ItemNotFoundException.class, () -> repository.save(randomUser(), clinicalTrial));
  }

  @Test
  void shouldFindClinicalTrialWithItsSubmissionsInOneRead() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    var project = clinicalTrial.projectIdentifier();
    repository.save(randomUser(), clinicalTrial);
    repository.save(randomUser(), submissionOf(project, randomPeriodIdentifier()));
    repository.save(randomUser(), submissionOf(project, randomPeriodIdentifier()));
    repository.save(
        randomUser(), submissionOf(randomProjectIdentifier(), randomPeriodIdentifier()));

    var reported = repository.findReportedTrial(project).orElseThrow();
    assertThat(reported.clinicalTrial().reportable(), is(clinicalTrial.reportable()));
    assertThat(reported.submissions(), hasSize(2));
  }

  @Test
  void shouldReturnEmptyListOfVersionsWhenProjectHasNoClinicalTrial() {
    assertThat(repository.findClinicalTrialVersions(randomProjectIdentifier()), is(List.of()));
  }

  @Test
  void shouldPersistVersionWithExpectedAttributeNames() throws Exception {
    var clinicalTrial = clinicalTrial(null);

    repository.save(randomUser(), clinicalTrial);

    var item = persistedVersion(clinicalTrial.projectIdentifier());
    assertThat(
        item.keySet(),
        is(Set.of("PK", "SK", "type", "entityType", "entity", "actor", "action", "modifiedDate")));
    assertThat(
        item.get("actor").m().keySet(),
        is(Set.of("kind", "username", "personIdentifier", "customerIdentifier", "grants")));
  }

  @Test
  void shouldReturnVersionWithCallerAndEntityWhenClinicalTrialIsSaved() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    var user = clinicalTrialAdmin();

    repository.save(user, clinicalTrial);

    var version =
        repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier()).getFirst();
    assertThat(version.actor(), is(user));
    assertThat(version.entity().projectIdentifier(), is(clinicalTrial.projectIdentifier()));
  }

  @Test
  void shouldRecordCreateAsActionOfFirstVersionAndUpdateOfNext() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);
    repository.save(randomUser(), persisted(clinicalTrial));

    var versions = repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier());

    assertThat(versions.getFirst().action(), is(Action.UPDATE));
    assertThat(versions.getLast().action(), is(Action.CREATE));
  }

  @Test
  void shouldReturnVersionsNewestFirstWhenClinicalTrialIsSavedTwice() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);
    repository.save(randomUser(), persisted(clinicalTrial));

    var versions = repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier());

    assertThat(versions.getFirst().entity().version(), is(2L));
    assertThat(versions.getLast().entity().version(), is(1L));
  }

  private ClinicalTrial persisted(ClinicalTrial clinicalTrial) {
    return repository.findClinicalTrial(clinicalTrial.projectIdentifier()).orElseThrow();
  }

  private Map<String, AttributeValue> persistedItem(String projectIdentifier) {
    return client
        .getItem(
            GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(
                    Map.of(
                        "PK", string("PROJECT#" + projectIdentifier),
                        "SK", string("CLINICAL_TRIAL")))
                .build())
        .item();
  }

  private Map<String, AttributeValue> persistedVersion(String projectIdentifier) {
    return versionRows(projectIdentifier).getFirst();
  }

  private List<Map<String, AttributeValue>> versionRows(String projectIdentifier) {
    return rowsOf("HISTORY#PROJECT#" + projectIdentifier + "#CLINICAL_TRIAL");
  }

  private List<Map<String, AttributeValue>> rowsOf(String partitionKey) {
    return client
        .query(
            QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("PK = :partitionKey")
                .expressionAttributeValues(Map.of(":partitionKey", string(partitionKey)))
                .build())
        .items();
  }

  @Test
  void shouldKeyVersionRowByVersionNumber() throws Exception {
    var clinicalTrial = clinicalTrial(null);

    repository.save(randomUser(), clinicalTrial);

    assertThat(
        persistedVersion(clinicalTrial.projectIdentifier()).get("SK").s(),
        is("0000000000000000001"));
  }

  @Test
  void shouldKeepVersionRowsOutOfEntityPartition() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);

    repository.save(randomUser(), persisted(clinicalTrial));

    assertThat(rowsOf("PROJECT#" + clinicalTrial.projectIdentifier()), hasSize(1));
    assertThat(versionRows(clinicalTrial.projectIdentifier()), hasSize(2));
  }

  @Test
  void shouldOrderVersionsByVersionNumber() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);
    repository.save(randomUser(), persisted(clinicalTrial));

    repository.save(randomUser(), persisted(clinicalTrial));

    var versions = repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier());
    assertThat(
        versions.stream().map(version -> version.entity().version()).toList(),
        is(List.of(3L, 2L, 1L)));
  }

  @Test
  void shouldReportStoredVersionWhenUpdateFindsAnotherVersion() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(randomUser(), clinicalTrial);
    var outdated = new ClinicalTrial(clinicalTrial.projectIdentifier(), Reportable.NO, 7L);

    var failure =
        assertThrows(ConditionFailedException.class, () -> repository.save(randomUser(), outdated));

    assertThat(failure.storedVersion(), is(1L));
  }

  private Submission submissionOf(String projectIdentifier, String periodIdentifier) {
    return new Submission(
        projectIdentifier, periodIdentifier, List.of(), SubmissionStatus.SUBMITTED, null);
  }

  private AttributeValue string(String value) {
    return AttributeValue.builder().s(value).build();
  }
}
