package no.unit.nva.clinicaltrial.persistence.dao;

import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.PARTITION_KEY;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.SORT_KEY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@JsonTypeName("ClinicalTrial")
public record ClinicalTrialDao(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    Reportable reportable,
    Long version,
    Instant modifiedDate)
    implements VersionedDao<ClinicalTrial> {

  private static final String CLINICAL_TRIAL_SORT_KEY = "CLINICAL_TRIAL";

  public static ClinicalTrialDao from(
      ClinicalTrial clinicalTrial, long version, Instant modifiedDate) {
    return new ClinicalTrialDao(
        Dao.projectKeyOf(clinicalTrial.projectIdentifier()),
        CLINICAL_TRIAL_SORT_KEY,
        clinicalTrial.reportable(),
        version,
        modifiedDate);
  }

  public static Key keyOf(String projectIdentifier) {
    return Key.builder()
        .partitionValue(Dao.projectKeyOf(projectIdentifier))
        .sortValue(CLINICAL_TRIAL_SORT_KEY)
        .build();
  }

  @Override
  public ClinicalTrial toEntity() {
    return new ClinicalTrial(Dao.projectIdentifierOf(partitionKey), reportable, version);
  }
}
