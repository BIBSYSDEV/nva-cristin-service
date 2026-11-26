package no.unit.nva.clinicaltrial.persistence;

import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Write;
import no.unit.nva.clinicaltrial.persistence.dao.Dao;
import no.unit.nva.clinicaltrial.persistence.dao.PeriodDao;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;

public class PeriodRepository {

  private final transient VersionedTable table;

  public PeriodRepository(VersionedTable table) {
    this.table = table;
  }

  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    table.save(write);
  }

  public Optional<Period> find(String identifier) {
    return table
        .item(PeriodDao.keyOf(identifier))
        .map(document -> Dao.fromDocument(document, PeriodDao.class))
        .map(PeriodDao::toEntity);
  }

  public List<Version<Period>> findVersions(String identifier) {
    return table.versionsOf(PeriodDao.keyOf(identifier), Period.class);
  }
}
