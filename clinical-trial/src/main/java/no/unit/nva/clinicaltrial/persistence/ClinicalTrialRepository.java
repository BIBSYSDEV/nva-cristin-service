package no.unit.nva.clinicaltrial.persistence;

import java.time.Instant;
import java.util.Arrays;
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

public class ClinicalTrialRepository {

    private final transient DynamoDbClient client;
    private final transient String tableName;

    public ClinicalTrialRepository(DynamoDbClient client, String tableName) {
        this.client = client;
        this.tableName = tableName;
    }

    public void save(ClinicalTrialWrite clinicalTrialWrite) {
        var now = Instant.now();
        var version = SortableIdentifier.next();
        var clinicalTrial = new ClinicalTrial(clinicalTrialWrite.clinicalTrial().projectIdentifier(), version);
        var dao = ClinicalTrialDao.fromClinicalTrial(clinicalTrial, now);
        var versionDao = VersionDao.fromDao(dao, clinicalTrial, clinicalTrialWrite.user(), version, now);
        client.transactWriteItems(createRequest(dao, versionDao));
    }

    public Optional<ClinicalTrial> find(String projectIdentifier) {
        return findDao(projectIdentifier).map(ClinicalTrialDao::toClinicalTrial);
    }

    public List<Version<ClinicalTrial>> findVersions(String projectIdentifier) {
        return client.query(versionsRequest(projectIdentifier))
                   .items()
                   .stream()
                   .map(item -> VersionDao.fromItem(item, ClinicalTrial.class))
                   .map(VersionDao::toVersion)
                   .toList();
    }

    private TransactWriteItemsRequest createRequest(Dao... daos) {
        return TransactWriteItemsRequest.builder()
                   .transactItems(Arrays.stream(daos).map(this::toTransactWriteItem).toList())
                   .build();
    }

    private TransactWriteItem toTransactWriteItem(Dao dao) {
        return TransactWriteItem.builder().put(Put.builder().tableName(tableName).item(dao.toItem()).build()).build();
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
        return GetItemRequest.builder().tableName(tableName).key(ClinicalTrialDao.keyOf(projectIdentifier)).build();
    }
}
