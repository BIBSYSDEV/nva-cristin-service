package no.unit.nva.clinicaltrial.persistence.dao;

import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.KEY_SEPARATOR;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.PARTITION_KEY;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.SORT_KEY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import java.util.List;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@JsonTypeName("Submission")
public record SubmissionDao(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    List<Inclusion> inclusions,
    SubmissionStatus status,
    Long version,
    Instant modifiedDate)
    implements VersionedDao<Submission> {

  private static final String SUBMISSION_KEY_PREFIX = "SUBMISSION" + KEY_SEPARATOR;

  public static SubmissionDao from(Submission submission, long version, Instant modifiedDate) {
    return new SubmissionDao(
        Dao.projectKeyOf(submission.projectIdentifier()),
        sortKeyOf(submission.periodIdentifier()),
        submission.inclusions(),
        submission.status(),
        version,
        modifiedDate);
  }

  public static Key keyOf(String projectIdentifier, String periodIdentifier) {
    return Key.builder()
        .partitionValue(Dao.projectKeyOf(projectIdentifier))
        .sortValue(sortKeyOf(periodIdentifier))
        .build();
  }

  @Override
  public Submission toEntity() {
    return new Submission(
        Dao.projectIdentifierOf(partitionKey),
        periodIdentifierOf(sortKey),
        inclusions,
        status,
        version);
  }

  private static String sortKeyOf(String periodIdentifier) {
    return SUBMISSION_KEY_PREFIX + periodIdentifier;
  }

  private static String periodIdentifierOf(String sortKey) {
    return sortKey.substring(SUBMISSION_KEY_PREFIX.length());
  }
}
