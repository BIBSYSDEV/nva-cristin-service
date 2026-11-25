package no.unit.nva.clinicaltrial.persistence;

import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Write;
import no.unit.nva.clinicaltrial.persistence.dao.ClinicalTrialDao;
import no.unit.nva.clinicaltrial.persistence.dao.Dao;
import no.unit.nva.clinicaltrial.persistence.dao.PeriodDao;
import no.unit.nva.clinicaltrial.persistence.dao.SubmissionDao;
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

  public Optional<ClinicalTrial> findClinicalTrial(String projectIdentifier) {
    return table
        .item(ClinicalTrialDao.keyOf(projectIdentifier))
        .map(document -> Dao.fromDocument(document, ClinicalTrialDao.class))
        .map(ClinicalTrialDao::toEntity);
  }

  public List<Version<ClinicalTrial>> findClinicalTrialVersions(String projectIdentifier) {
    return table.versionsOf(ClinicalTrialDao.keyOf(projectIdentifier), ClinicalTrial.class);
  }

  public Optional<Period> findPeriod(String periodIdentifier) {
    return table
        .item(PeriodDao.keyOf(periodIdentifier))
        .map(document -> Dao.fromDocument(document, PeriodDao.class))
        .map(PeriodDao::toEntity);
  }

  public List<Version<Period>> findPeriodVersions(String periodIdentifier) {
    return table.versionsOf(PeriodDao.keyOf(periodIdentifier), Period.class);
  }

  public Optional<Submission> findSubmission(String projectIdentifier, String periodIdentifier) {
    return table
        .item(SubmissionDao.keyOf(projectIdentifier, periodIdentifier))
        .map(document -> Dao.fromDocument(document, SubmissionDao.class))
        .map(SubmissionDao::toEntity);
  }

  public List<Version<Submission>> findSubmissionVersions(
      String projectIdentifier, String periodIdentifier) {
    return table.versionsOf(
        SubmissionDao.keyOf(projectIdentifier, periodIdentifier), Submission.class);
  }
}
