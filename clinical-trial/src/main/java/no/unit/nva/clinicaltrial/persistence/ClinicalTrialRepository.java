package no.unit.nva.clinicaltrial.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public class ClinicalTrialRepository {

  private final transient DynamoDbClient client;
  private final transient String tableName;

  public ClinicalTrialRepository(DynamoDbClient client, String tableName) {
    this.client = client;
    this.tableName = tableName;
  }

  public void save(ClinicalTrialWrite clinicalTrialWrite) throws ConditionFailedException {
    var now = Instant.now();
    var expectedVersion = clinicalTrialWrite.clinicalTrial().version();
    var clinicalTrial = nextVersionOf(clinicalTrialWrite.clinicalTrial());
    var dao = daoToPersist(clinicalTrial, expectedVersion, now);
    var versionDao =
        VersionDao.fromDao(
            dao, clinicalTrial, clinicalTrialWrite.user(), clinicalTrial.version(), now);
    try {
      client.transactWriteItems(saveRequest(dao, versionDao, expectedVersion));
    } catch (TransactionCanceledException conditionNotMet) {
      throw new ConditionFailedException();
    }
  }

  public Optional<ClinicalTrial> find(String projectIdentifier) {
    return findDao(projectIdentifier).map(ClinicalTrialDao::toClinicalTrial);
  }

  public List<Version<ClinicalTrial>> findVersions(String projectIdentifier) {
    return client.query(versionsRequest(projectIdentifier)).items().stream()
        .map(item -> VersionDao.fromItem(item, ClinicalTrial.class))
        .map(VersionDao::toVersion)
        .toList();
  }

  private static ClinicalTrial nextVersionOf(ClinicalTrial clinicalTrial) {
    return new ClinicalTrial(clinicalTrial.projectIdentifier(), SortableIdentifier.next());
  }

  private TransactWriteItemsRequest saveRequest(
      ClinicalTrialDao dao,
      VersionDao<ClinicalTrial> versionDao,
      SortableIdentifier expectedVersion) {
    return TransactWriteItemsRequest.builder()
        .transactItems(clinicalTrialItem(dao, expectedVersion), versionItem(versionDao))
        .build();
  }

  private ClinicalTrialDao daoToPersist(
      ClinicalTrial clinicalTrial, SortableIdentifier expectedVersion, Instant timestamp) {
    return Optional.ofNullable(expectedVersion)
        .flatMap(version -> findDao(clinicalTrial.projectIdentifier()))
        .map(persisted -> persisted.updatedTo(clinicalTrial, timestamp))
        .orElseGet(() -> ClinicalTrialDao.fromClinicalTrial(clinicalTrial, timestamp));
  }

  private TransactWriteItem clinicalTrialItem(
      ClinicalTrialDao dao, SortableIdentifier expectedVersion) {
    return Optional.ofNullable(expectedVersion)
        .map(version -> updatedClinicalTrialItem(dao, version))
        .orElseGet(() -> createdClinicalTrialItem(dao));
  }

  private TransactWriteItem createdClinicalTrialItem(ClinicalTrialDao dao) {
    return transactItem(
        itemToPut(dao).conditionExpression(ClinicalTrialDao.CREATE_CONDITION).build());
  }

  private TransactWriteItem updatedClinicalTrialItem(
      ClinicalTrialDao dao, SortableIdentifier expectedVersion) {
    return transactItem(
        itemToPut(dao)
            .conditionExpression(ClinicalTrialDao.UPDATE_CONDITION)
            .expressionAttributeValues(ClinicalTrialDao.expectedVersionValues(expectedVersion))
            .build());
  }

  private TransactWriteItem versionItem(Dao dao) {
    return transactItem(itemToPut(dao).build());
  }

  private Put.Builder itemToPut(Dao dao) {
    return Put.builder().tableName(tableName).item(dao.toItem());
  }

  private static TransactWriteItem transactItem(Put put) {
    return TransactWriteItem.builder().put(put).build();
  }

  private Optional<ClinicalTrialDao> findDao(String projectIdentifier) {
    var response = client.getItem(getItemRequest(projectIdentifier));
    return Optional.of(response)
        .filter(GetItemResponse::hasItem)
        .map(GetItemResponse::item)
        .map(ClinicalTrialDao::fromItem);
  }

  private QueryRequest versionsRequest(String projectIdentifier) {
    return QueryRequest.builder()
        .tableName(tableName)
        .keyConditionExpression(VersionDao.VERSIONS_KEY_CONDITION)
        .expressionAttributeValues(
            VersionDao.versionsConditionValues(ClinicalTrialDao.keyOf(projectIdentifier)))
        .scanIndexForward(false)
        .build();
  }

  private GetItemRequest getItemRequest(String projectIdentifier) {
    return GetItemRequest.builder()
        .tableName(tableName)
        .key(ClinicalTrialDao.keyOf(projectIdentifier))
        .build();
  }
}
