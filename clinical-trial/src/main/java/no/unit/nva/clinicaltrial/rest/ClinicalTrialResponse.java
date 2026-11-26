package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import java.util.List;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWithSubmissions;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.Submission;
import nva.commons.core.paths.UriWrapper;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonTypeName("ClinicalTrial")
public record ClinicalTrialResponse(
    @JsonProperty("@context") URI context,
    URI id,
    URI project,
    Reportable reportable,
    List<SubmissionSummary> submissions) {

  private static final String CLINICAL_TRIAL_PATH = "clinical-trial";

  public static ClinicalTrialResponse from(ClinicalTrialWithSubmissions reported) {
    var clinicalTrial = reported.clinicalTrial();
    var project = getNvaApiId(clinicalTrial.projectIdentifier(), PROJECT_PATH_NVA);
    return new ClinicalTrialResponse(
        URI.create(PROJECT_LOOKUP_CONTEXT_URL),
        trialId(project),
        project,
        clinicalTrial.reportable(),
        summariesOf(trialId(project), reported.submissions()));
  }

  private static URI trialId(URI project) {
    return UriWrapper.fromUri(project).addChild(CLINICAL_TRIAL_PATH).getUri();
  }

  private static List<SubmissionSummary> summariesOf(URI trialId, List<Submission> submissions) {
    return submissions.stream().map(submission -> summaryOf(trialId, submission)).toList();
  }

  private static SubmissionSummary summaryOf(URI trialId, Submission submission) {
    return new SubmissionSummary(
        UriWrapper.fromUri(trialId).addChild(submission.periodIdentifier()).getUri(),
        submission.periodIdentifier(),
        submission.status());
  }
}
