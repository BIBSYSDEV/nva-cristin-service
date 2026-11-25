package no.unit.nva.clinicaltrial.persistence.v2;

import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.Write;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;

public class ClinicalTrialRepository {

  private final transient VersionedTable table;
  private final transient DynamoDbTable<ClinicalTrialDao> clinicalTrials;

  public ClinicalTrialRepository(VersionedTable table) {
    this.table = table;
    this.clinicalTrials = table.clinicalTrialTable();
  }

  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    table.save(write);
  }

  public Optional<ClinicalTrial> find(String projectIdentifier) {
    return Optional.ofNullable(clinicalTrials.getItem(readRequest(projectIdentifier)))
        .map(ClinicalTrialDao::toEntity);
  }

  public List<Version<ClinicalTrial>> findVersions(String projectIdentifier) {
    return table.versionsOf(ClinicalTrialDao.keyOf(projectIdentifier), ClinicalTrial.class);
  }

  private static GetItemEnhancedRequest readRequest(String projectIdentifier) {
    return GetItemEnhancedRequest.builder()
        .key(ClinicalTrialDao.keyFor(projectIdentifier))
        .consistentRead(true)
        .build();
  }
}
