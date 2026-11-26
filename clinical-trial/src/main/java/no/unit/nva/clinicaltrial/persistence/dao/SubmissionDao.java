package no.unit.nva.clinicaltrial.persistence.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
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
    Instant createdDate,
    Instant modifiedDate)
    implements VersionedDao<Submission> {

  private static final String SUBMISSION_KEY_PREFIX = "SUBMISSION#";

  public static SubmissionDao from(Submission submission, Instant timestamp) {
    return new SubmissionDao(
        Dao.projectKeyOf(submission.projectIdentifier()),
        sortKeyOf(submission.periodIdentifier()),
        submission.inclusions(),
        submission.status(),
        submission.version(),
        Objects.isNull(submission.version()) ? timestamp : null,
        timestamp);
  }

  public static Key keyOf(String projectIdentifier, String periodIdentifier) {
    return Key.builder()
        .partitionValue(Dao.projectKeyOf(projectIdentifier))
        .sortValue(sortKeyOf(periodIdentifier))
        .build();
  }

  @Override
  public Submission entityAtVersion(Long version) {
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
