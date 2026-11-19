package no.unit.nva.clinicaltrial.persistence;

import java.time.Instant;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

public class ClinicalTrialRepository {

  private final transient DynamoDbClient client;
  private final transient String tableName;

  public ClinicalTrialRepository(DynamoDbClient client, String tableName) {
    this.client = client;
    this.tableName = tableName;
  }

  public void save(ClinicalTrialWrite clinicalTrialWrite) {
    var now = Instant.now();
    var dao = ClinicalTrialDao.fromClinicalTrial(clinicalTrialWrite.clinicalTrial(), now);
    client.transactWriteItems(createRequest(dao));
  }

  private TransactWriteItemsRequest createRequest(ClinicalTrialDao dao) {
    return TransactWriteItemsRequest.builder()
        .transactItems(dao.toTransactWriteItem(tableName))
        .build();
  }

  public Optional<ClinicalTrial> find(String projectIdentifier) {
    var response = client.getItem(getItemRequest(projectIdentifier));
    return Optional.of(response)
        .filter(GetItemResponse::hasItem)
        .map(GetItemResponse::item)
        .map(ClinicalTrialDao::fromItem)
        .map(ClinicalTrialDao::toClinicalTrial);
  }

  private GetItemRequest getItemRequest(String projectIdentifier) {
    return GetItemRequest.builder()
        .tableName(tableName)
        .key(ClinicalTrialDao.keyOf(projectIdentifier))
        .build();
  }
}
