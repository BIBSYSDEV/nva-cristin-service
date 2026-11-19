package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomClinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

class ClinicalTrialRepositoryTest {

  private static final String TABLE_NAME = "clinical-trial";

  private DynamoDbClient client;
  private ClinicalTrialRepository repository;

  @BeforeEach
  void setUp() {
    client = LocalDynamoDbTable.create(TABLE_NAME);
    repository = new ClinicalTrialRepository(client, TABLE_NAME);
  }

  @Test
  void shouldSaveClinicalTrial() {
    var clinicalTrial = randomClinicalTrial();

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(repository.find(clinicalTrial.projectIdentifier()).orElseThrow(), is(clinicalTrial));
  }

  @Test
  void shouldReturnOptionalEmptyWhenProjectHasNoClinicalTrial() {
    assertThat(repository.find(randomProjectIdentifier()), is(Optional.empty()));
  }

  @Test
  void shouldStoreClinicalTrialWithExpectedAttributeNames() {
    var clinicalTrial = randomClinicalTrial();

    repository.save(new ClinicalTrialWrite(clinicalTrial, randomUser()));

    assertThat(
        storedItem(clinicalTrial.projectIdentifier()).keySet(),
        is(Set.of("PK", "SK", "createdDate", "modifiedDate")));
  }

  private Map<String, AttributeValue> storedItem(String projectIdentifier) {
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

  private AttributeValue string(String value) {
    return AttributeValue.builder().s(value).build();
  }
}
