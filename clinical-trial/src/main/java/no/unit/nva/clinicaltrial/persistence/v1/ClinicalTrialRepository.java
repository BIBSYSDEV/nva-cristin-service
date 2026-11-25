package no.unit.nva.clinicaltrial.persistence.v1;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.Write;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class ClinicalTrialRepository {

  private final transient VersionedTable table;

  public ClinicalTrialRepository(VersionedTable table) {
    this.table = table;
  }

  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    table.save(write);
  }

  public Optional<ClinicalTrial> find(String projectIdentifier) {
    return Optional.of(table.item(ClinicalTrialDao.keyOf(projectIdentifier)))
        .filter(item -> !item.isEmpty())
        .map(ClinicalTrialRepository::toDao)
        .map(ClinicalTrialDao::toEntity);
  }

  public List<Version<ClinicalTrial>> findVersions(String projectIdentifier) {
    return table.versionsOf(ClinicalTrialDao.keyOf(projectIdentifier), ClinicalTrial.class);
  }

  private static ClinicalTrialDao toDao(Map<String, AttributeValue> item) {
    return Dao.fromItem(item, ClinicalTrialDao.class);
  }
}
