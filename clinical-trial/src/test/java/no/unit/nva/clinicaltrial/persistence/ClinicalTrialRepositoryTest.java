package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrialAdmin;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.write.ClinicalTrialWrite;
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
  private static final String VERSION_KEY_PREFIX = "VERSION#CLINICAL_TRIAL#";

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

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(persisted(clinicalTrial).version(), is(1L));
  }

  @Test
  void shouldReturnOptionalEmptyWhenProjectHasNoClinicalTrial() {
    assertThat(repository.findClinicalTrial(randomProjectIdentifier()), is(Optional.empty()));
  }

  @Test
  void shouldPersistClinicalTrialWithExpectedAttributeNames() throws Exception {
    var clinicalTrial = clinicalTrial(null);

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(
        persistedItem(clinicalTrial.projectIdentifier()).keySet(),
        is(Set.of("PK", "SK", "type", "reportable", "version", "createdDate", "modifiedDate")));
  }

  @Test
  void shouldKeepCreatedDateAndRaiseVersionWhenClinicalTrialIsUpdated() throws Exception {
    var clinicalTrial = clinicalTrial(Reportable.YES, null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    var createdDate = persistedItem(clinicalTrial.projectIdentifier()).get("createdDate").s();

    repository.save(new ClinicalTrialWrite(persisted(clinicalTrial), randomUser()));

    var item = persistedItem(clinicalTrial.projectIdentifier());
    assertThat(item.get("createdDate").s(), is(createdDate));
    assertThat(item.get("version").n(), is("2"));
  }

  @Test
  void shouldPersistChangedReportableWhenClinicalTrialIsUpdated() throws Exception {
    var clinicalTrial = clinicalTrial(Reportable.YES, null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    var persisted = persisted(clinicalTrial);

    repository.save(
        new ClinicalTrialWrite(
            new ClinicalTrial(persisted.projectIdentifier(), Reportable.NO, persisted.version()),
            randomUser()));

    assertThat(persistedItem(clinicalTrial.projectIdentifier()).get("reportable").s(), is("No"));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenCreatingClinicalTrialForProjectThatAlreadyHasOne()
      throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThrows(
        ConditionFailedException.class,
        () -> repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser())));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenUpdatingClinicalTrialWithOutdatedVersion()
      throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    var outdated = new ClinicalTrial(clinicalTrial.projectIdentifier(), Reportable.NO, 7L);

    assertThrows(
        ConditionFailedException.class,
        () -> repository.save(new ClinicalTrialWrite(outdated, randomUser())));
  }

  @Test
  void shouldThrowItemNotFoundExceptionWhenUpdatingNonExistingClinicalTrial() {
    var clinicalTrial = clinicalTrial(Reportable.YES, 1L);

    assertThrows(
        ItemNotFoundException.class,
        () -> repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser())));
  }

  @Test
  void shouldReturnEmptyListOfVersionsWhenProjectHasNoClinicalTrial() {
    assertThat(repository.findClinicalTrialVersions(randomProjectIdentifier()), is(List.of()));
  }

  @Test
  void shouldPersistVersionWithExpectedAttributeNames() throws Exception {
    var clinicalTrial = clinicalTrial(null);

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    var item = persistedVersion(clinicalTrial.projectIdentifier());
    assertThat(
        item.keySet(),
        is(
            Set.of(
                "PK", "SK", "type", "entity", "writer", "action", "createdDate", "modifiedDate")));
    assertThat(
        item.get("writer").m().keySet(),
        is(Set.of("kind", "username", "personIdentifier", "customerIdentifier", "grants")));
  }

  @Test
  void shouldReturnVersionWithCallerAndEntityWhenClinicalTrialIsSaved() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    var user = clinicalTrialAdmin();

    repository.save(new ClinicalTrialWrite(clinicalTrial, user));

    var version =
        repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier()).getFirst();
    assertThat(version.writer(), is(user));
    assertThat(version.entity().projectIdentifier(), is(clinicalTrial.projectIdentifier()));
  }

  @Test
  void shouldRecordCreateAsActionOfFirstVersionAndUpdateOfNext() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    repository.save(new ClinicalTrialWrite(persisted(clinicalTrial), randomUser()));

    var versions = repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier());

    assertThat(versions.getFirst().action(), is(Action.UPDATE_CLINICAL_TRIAL));
    assertThat(versions.getLast().action(), is(Action.CREATE_CLINICAL_TRIAL));
  }

  @Test
  void shouldReturnVersionsNewestFirstWhenClinicalTrialIsSavedTwice() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    repository.save(new ClinicalTrialWrite(persisted(clinicalTrial), randomUser()));

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
    return client
        .query(
            QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression("PK = :partitionKey AND begins_with(SK, :versionPrefix)")
                .expressionAttributeValues(
                    Map.of(
                        ":partitionKey", string("PROJECT#" + projectIdentifier),
                        ":versionPrefix", string(VERSION_KEY_PREFIX)))
                .build())
        .items()
        .getFirst();
  }

  private AttributeValue string(String value) {
    return AttributeValue.builder().s(value).build();
  }
}
