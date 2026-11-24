package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.model.Action.CREATE_CLINICAL_TRIAL;
import static no.unit.nva.clinicaltrial.model.Action.UPDATE_CLINICAL_TRIAL;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.CREATE_CONDITION;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.EXPECTED_VERSION_VALUE;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.UPDATE_CONDITION;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.VERSIONS_KEY_CONDITION;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
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

  public ClinicalTrial save(ClinicalTrialWrite clinicalTrialWrite)
      throws ConditionFailedException, ClinicalTrialNotFoundException {
    var now = Instant.now();
    var expectedVersion = clinicalTrialWrite.clinicalTrial().version();
    var clinicalTrial = nextVersionOf(clinicalTrialWrite.clinicalTrial());
    var dao = daoToPersist(clinicalTrial, expectedVersion, now);
    var versionDao =
        VersionDao.newVersionOf(
            dao, clinicalTrial, clinicalTrialWrite.user(), action(expectedVersion));
    try {
      client.transactWriteItems(saveRequest(dao, versionDao, expectedVersion));
    } catch (TransactionCanceledException transactionCanceled) {
      throw ConditionFailedException.from(transactionCanceled);
    }
    return clinicalTrial;
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

  private static Action action(SortableIdentifier expectedVersion) {
    return Objects.isNull(expectedVersion) ? CREATE_CLINICAL_TRIAL : UPDATE_CLINICAL_TRIAL;
  }

  private static ClinicalTrial nextVersionOf(ClinicalTrial clinicalTrial) {
    return new ClinicalTrial(
        clinicalTrial.projectIdentifier(), clinicalTrial.reportable(), SortableIdentifier.next());
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
      ClinicalTrial clinicalTrial, SortableIdentifier expectedVersion, Instant timestamp)
      throws ClinicalTrialNotFoundException {
    return Objects.isNull(expectedVersion)
        ? ClinicalTrialDao.fromClinicalTrial(clinicalTrial, timestamp)
        : persistedDao(clinicalTrial.projectIdentifier()).updatedTo(clinicalTrial, timestamp);
  }

  private ClinicalTrialDao persistedDao(String projectIdentifier)
      throws ClinicalTrialNotFoundException {
    return findDao(projectIdentifier).orElseThrow(ClinicalTrialNotFoundException::new);
  }

  private TransactWriteItem clinicalTrialItem(
      ClinicalTrialDao dao, SortableIdentifier expectedVersion) {
    return Optional.ofNullable(expectedVersion)
        .map(version -> updatedClinicalTrialItem(dao, version))
        .orElseGet(() -> createdClinicalTrialItem(dao));
  }

  private TransactWriteItem createdClinicalTrialItem(ClinicalTrialDao dao) {
    return transactItem(itemToPut(dao).conditionExpression(CREATE_CONDITION).build());
  }

  private TransactWriteItem updatedClinicalTrialItem(
      ClinicalTrialDao dao, SortableIdentifier expectedVersion) {
    return transactItem(
        itemToPut(dao)
            .conditionExpression(UPDATE_CONDITION)
            .expressionAttributeValues(expectedVersionValues(expectedVersion))
            .build());
  }

  private TransactWriteItem versionItem(Dao dao) {
    return transactItem(itemToPut(dao).build());
  }

  private Put.Builder itemToPut(Dao dao) {
    return Put.builder().tableName(tableName).item(dao.toItem());
  }

  private static Map<String, AttributeValue> expectedVersionValues(
      SortableIdentifier expectedVersion) {
    return Map.of(EXPECTED_VERSION_VALUE, AttributeValue.fromS(expectedVersion.toString()));
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
        .keyConditionExpression(VERSIONS_KEY_CONDITION)
        .expressionAttributeValues(
            VersionDao.versionsConditionValues(ClinicalTrialDao.keyOf(projectIdentifier)))
        .scanIndexForward(false)
        .build();
  }

  private GetItemRequest getItemRequest(String projectIdentifier) {
    return GetItemRequest.builder()
        .tableName(tableName)
        .key(ClinicalTrialDao.keyOf(projectIdentifier))
        .consistentRead(true)
        .build();
  }
}
