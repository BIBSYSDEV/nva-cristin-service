package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import java.util.List;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import nva.commons.core.paths.UriWrapper;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonTypeName("Submission")
public record SubmissionResponse(
    @JsonProperty("@context") URI context,
    URI id,
    URI project,
    String period,
    SubmissionStatus status,
    List<InclusionResponse> inclusions) {

  private static final String CLINICAL_TRIAL_PATH = "clinical-trial";

  public static SubmissionResponse from(Submission submission) {
    var project = getNvaApiId(submission.projectIdentifier(), PROJECT_PATH_NVA);
    return new SubmissionResponse(
        URI.create(PROJECT_LOOKUP_CONTEXT_URL),
        submissionId(project, submission.periodIdentifier()),
        project,
        submission.periodIdentifier(),
        submission.status(),
        inclusionsOf(submission));
  }

  private static URI submissionId(URI project, String periodIdentifier) {
    return UriWrapper.fromUri(project)
        .addChild(CLINICAL_TRIAL_PATH)
        .addChild(periodIdentifier)
        .getUri();
  }

  private static List<InclusionResponse> inclusionsOf(Submission submission) {
    return submission.inclusions().stream().map(SubmissionResponse::inclusionOf).toList();
  }

  private static InclusionResponse inclusionOf(Inclusion inclusion) {
    return new InclusionResponse(
        getNvaApiId(inclusion.organization().value(), ORGANIZATION_PATH), inclusion.count());
  }
}
