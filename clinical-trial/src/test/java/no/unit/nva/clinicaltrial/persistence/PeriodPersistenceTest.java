package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.write.System;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

class PeriodPersistenceTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String ADMIN_PROCESS = "period-administration";

  private DynamoDbClient client;
  private ClinicalTrialRepository repository;

  @BeforeEach
  void setUp() {
    client = LocalDynamoDbTable.create(TABLE_NAME);
    repository = LocalDynamoDbTable.repositoryOf(client, TABLE_NAME);
  }

  @Test
  void shouldFailWhenStoredPeriodCarriesNoVersion() {
    var identifier = randomString();
    client.putItem(
        PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(
                Map.of(
                    "PK", AttributeValue.fromS("PERIOD"),
                    "SK", AttributeValue.fromS(identifier)))
            .build());

    assertThrows(IllegalStateException.class, () -> repository.findPeriod(identifier));
  }

  @Test
  void shouldFindPersistedPeriod() throws Exception {
    var period = new Period(randomString(), null);

    repository.save(new System(ADMIN_PROCESS), period);

    assertThat(
        repository.findPeriod(period.identifier()).orElseThrow().identifier(),
        is(period.identifier()));
  }

  @Test
  void shouldPersistPeriodWithExpectedAttributeNames() throws Exception {
    var period = new Period(randomString(), null);

    repository.save(new System(ADMIN_PROCESS), period);

    assertThat(
        persistedItem(period.identifier()).keySet(),
        is(Set.of("PK", "SK", "type", "version", "modifiedDate")));
  }

  @Test
  void shouldListVersionsOfPeriodNewestFirst() throws Exception {
    var period = new Period(randomString(), null);
    repository.save(new System(ADMIN_PROCESS), period);
    var persisted = repository.findPeriod(period.identifier()).orElseThrow();

    repository.save(new System(ADMIN_PROCESS), persisted);

    var versions = repository.findPeriodVersions(period.identifier());
    assertThat(versions.getFirst().entity().version(), is(2L));
    assertThat(versions.getLast().action(), is(Action.CREATE));
  }

  private Map<String, AttributeValue> persistedItem(String identifier) {
    return client
        .getItem(
            GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(
                    Map.of(
                        "PK", AttributeValue.fromS("PERIOD"),
                        "SK", AttributeValue.fromS(identifier)))
                .build())
        .item();
  }
}
