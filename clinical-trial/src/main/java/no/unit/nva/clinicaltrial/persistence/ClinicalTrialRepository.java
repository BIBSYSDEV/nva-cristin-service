package no.unit.nva.clinicaltrial.persistence;

import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Write;
import no.unit.nva.clinicaltrial.persistence.dao.ClinicalTrialDao;
import no.unit.nva.clinicaltrial.persistence.dao.Dao;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;

public class ClinicalTrialRepository {

  private final transient VersionedTable table;

  public ClinicalTrialRepository(VersionedTable table) {
    this.table = table;
  }

  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    table.save(write);
  }

  public Optional<ClinicalTrial> find(String projectIdentifier) {
    return table
        .item(ClinicalTrialDao.keyOf(projectIdentifier))
        .map(document -> Dao.fromDocument(document, ClinicalTrialDao.class))
        .map(ClinicalTrialDao::toEntity);
  }

  public List<Version<ClinicalTrial>> findVersions(String projectIdentifier) {
    return table.versionsOf(ClinicalTrialDao.keyOf(projectIdentifier), ClinicalTrial.class);
  }
}
