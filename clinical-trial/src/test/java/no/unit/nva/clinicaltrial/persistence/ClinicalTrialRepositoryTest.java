package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.identifiers.SortableIdentifier;
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
    repository = new ClinicalTrialRepository(client, TABLE_NAME);
  }

  @Test
  void shouldSaveClinicalTrial() throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(
        repository.find(clinicalTrial.projectIdentifier()).orElseThrow().projectIdentifier(),
        is(clinicalTrial.projectIdentifier()));
  }

  @Test
  void shouldReturnOptionalEmptyWhenProjectHasNoClinicalTrial() {
    assertThat(repository.find(randomProjectIdentifier()), is(Optional.empty()));
  }

  @Test
  void shouldPersistClinicalTrialWithExpectedAttributeNames() throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(
        persistedItem(clinicalTrial.projectIdentifier()).keySet(),
        is(Set.of("PK", "SK", "version", "createdDate", "modifiedDate")));
  }

  @Test
  void shouldPersistVersionWithExpectedAttributeNames() throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(
        persistedVersion(clinicalTrial.projectIdentifier()).keySet(),
        is(
            Set.of(
                "PK",
                "SK",
                "entity",
                "username",
                "customerIdentifier",
                "createdDate",
                "modifiedDate")));
  }

  @Test
  void shouldReturnEmptyListOfVersionsWhenProjectHasNoClinicalTrial() {
    assertThat(repository.findVersions(randomProjectIdentifier()), is(List.of()));
  }

  @Test
  void shouldReturnVersionWithCallerAndEntityWhenClinicalTrialIsSaved()
      throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);
    var user = randomUser();

    repository.save(new ClinicalTrialWrite(clinicalTrial, user));

    var version = repository.findVersions(clinicalTrial.projectIdentifier()).getFirst();
    assertThat(version.user(), is(user));
    assertThat(version.entity().projectIdentifier(), is(clinicalTrial.projectIdentifier()));
  }

  @Test
  void shouldReturnVersionsNewestFirstWhenClinicalTrialIsSavedTwice()
      throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    repository.save(new ClinicalTrialWrite(persisted(clinicalTrial), randomUser()));

    var versions = repository.findVersions(clinicalTrial.projectIdentifier());

    assertThat(versions.size(), is(2));
    assertThat(
        versions.getFirst().identifier().toString(),
        is(persistedItem(clinicalTrial.projectIdentifier()).get("version").s()));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenCreatingClinicalTrialForProjectThatAlreadyHasOne()
      throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThrows(
        ConditionFailedException.class,
        () -> repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser())));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenUpdatingClinicalTrialWithOutdatedVersion()
      throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    var outdated = new ClinicalTrial(clinicalTrial.projectIdentifier(), SortableIdentifier.next());

    assertThrows(
        ConditionFailedException.class,
        () -> repository.save(new ClinicalTrialWrite(outdated, randomUser())));
  }

  @Test
  void shouldThrowConditionFailedExceptionWhenUpdatingNonExistingClinicalTrial() {
    var clinicalTrial = clinicalTrial(SortableIdentifier.next());

    assertThrows(
        ConditionFailedException.class,
        () -> repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser())));
  }

  @Test
  void shouldKeepCreatedDateWhenClinicalTrialIsUpdated() throws ConditionFailedException {
    var clinicalTrial = clinicalTrial(null);
    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));
    var createdDate = persistedItem(clinicalTrial.projectIdentifier()).get("createdDate").s();

    repository.save(new ClinicalTrialWrite(persisted(clinicalTrial), randomUser()));

    assertThat(
        persistedItem(clinicalTrial.projectIdentifier()).get("createdDate").s(), is(createdDate));
  }

  private ClinicalTrial persisted(ClinicalTrial clinicalTrial) {
    return repository.find(clinicalTrial.projectIdentifier()).orElseThrow();
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
