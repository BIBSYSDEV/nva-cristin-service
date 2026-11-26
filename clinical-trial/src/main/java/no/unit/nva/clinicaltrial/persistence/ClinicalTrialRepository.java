package no.unit.nva.clinicaltrial.persistence;

import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Actor;
import no.unit.nva.clinicaltrial.persistence.dao.ClinicalTrialDao;
import no.unit.nva.clinicaltrial.persistence.dao.PeriodDao;
import no.unit.nva.clinicaltrial.persistence.dao.SubmissionDao;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class ClinicalTrialRepository {

  private static final String TABLE_NAME = "TABLE_NAME";

  private final transient VersionedTable table;

  public ClinicalTrialRepository(VersionedTable table) {
    this.table = table;
  }

  @JacocoGenerated
  public static ClinicalTrialRepository defaultRepository() {
    return new ClinicalTrialRepository(
        new VersionedTable(
            DynamoDbEnhancedClient.builder().dynamoDbClient(DynamoDbClient.create()).build(),
            new Environment().readEnv(TABLE_NAME)));
  }

  public void save(Actor actor, Entity... entities)
      throws ConditionFailedException, ItemNotFoundException {
    table.save(actor, entities);
  }

  public Optional<ClinicalTrial> findClinicalTrial(String projectIdentifier) {
    return table.item(ClinicalTrialDao.keyOf(projectIdentifier), ClinicalTrialDao.class);
  }

  public List<Version<ClinicalTrial>> findClinicalTrialVersions(String projectIdentifier) {
    return table.versionsOf(ClinicalTrialDao.keyOf(projectIdentifier), ClinicalTrial.class);
  }

  public Optional<Period> findPeriod(String periodIdentifier) {
    return table.item(PeriodDao.keyOf(periodIdentifier), PeriodDao.class);
  }

  public List<Version<Period>> findPeriodVersions(String periodIdentifier) {
    return table.versionsOf(PeriodDao.keyOf(periodIdentifier), Period.class);
  }

  public Optional<Submission> findSubmission(String projectIdentifier, String periodIdentifier) {
    return table.item(
        SubmissionDao.keyOf(projectIdentifier, periodIdentifier), SubmissionDao.class);
  }

  public List<Version<Submission>> findSubmissionVersions(
      String projectIdentifier, String periodIdentifier) {
    return table.versionsOf(
        SubmissionDao.keyOf(projectIdentifier, periodIdentifier), Submission.class);
  }
}
