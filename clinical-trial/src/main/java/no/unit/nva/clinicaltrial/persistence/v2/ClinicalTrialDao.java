package no.unit.nva.clinicaltrial.persistence.v2;

import java.time.Instant;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class ClinicalTrialDao extends VersionedDao<ClinicalTrial> {

  private static final String PROJECT_KEY_PREFIX = "PROJECT#";
  private static final String CLINICAL_TRIAL_SORT_KEY = "CLINICAL_TRIAL";

  private Reportable reportable;

  public static ClinicalTrialDao from(ClinicalTrial clinicalTrial, Instant timestamp) {
    var dao = keyOf(clinicalTrial.identifier());
    dao.setReportable(clinicalTrial.reportable());
    dao.setVersion(clinicalTrial.version());
    dao.timestamp(timestamp);
    return dao;
  }

  public static ClinicalTrialDao keyOf(String projectIdentifier) {
    var dao = new ClinicalTrialDao();
    dao.setPartitionKey(partitionKeyOf(projectIdentifier));
    dao.setSortKey(CLINICAL_TRIAL_SORT_KEY);
    return dao;
  }

  public static Key keyFor(String projectIdentifier) {
    return Key.builder()
        .partitionValue(partitionKeyOf(projectIdentifier))
        .sortValue(CLINICAL_TRIAL_SORT_KEY)
        .build();
  }

  @Override
  public ClinicalTrial toEntity() {
    return new ClinicalTrial(projectIdentifierOf(getPartitionKey()), reportable, getVersion());
  }

  public Reportable getReportable() {
    return reportable;
  }

  public void setReportable(Reportable reportable) {
    this.reportable = reportable;
  }

  private static String partitionKeyOf(String projectIdentifier) {
    return PROJECT_KEY_PREFIX + projectIdentifier;
  }

  private static String projectIdentifierOf(String partitionKey) {
    return partitionKey.substring(PROJECT_KEY_PREFIX.length());
  }
}
