package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPeriodIdentifier;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.net.URI;
import java.util.List;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWithSubmissions;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

class ClinicalTrialResponseTest {

  private static final String CLINICAL_TRIAL_PATH = "clinical-trial";

  @Test
  void shouldLinkToItselfAndToItsProject() {
    var clinicalTrial = clinicalTrial(1L);

    var response = ClinicalTrialResponse.from(reported(clinicalTrial));

    assertThat(response.id(), is(clinicalTrialId(clinicalTrial.projectIdentifier())));
    assertThat(response.project(), is(projectId(clinicalTrial.projectIdentifier())));
  }

  @Test
  void shouldCarryItsTypeName() {
    var response = ClinicalTrialResponse.from(reported(clinicalTrial(1L)));

    assertThat(dtoObjectMapper.valueToTree(response).get("type").asText(), is("ClinicalTrial"));
  }

  @Test
  void shouldCarryReportableValueAndProjectContext() {
    var response = ClinicalTrialResponse.from(reported(clinicalTrial(Reportable.NO, 1L)));

    assertThat(response.reportable(), is(Reportable.NO));
    assertThat(response.context(), is(URI.create(PROJECT_LOOKUP_CONTEXT_URL)));
  }

  @Test
  void shouldSummariseEachSubmissionReportedForTheTrial() {
    var clinicalTrial = clinicalTrial(1L);
    var submission =
        new Submission(
            clinicalTrial.projectIdentifier(),
            randomPeriodIdentifier(),
            List.of(),
            SubmissionStatus.SUBMITTED,
            1L);

    var response =
        ClinicalTrialResponse.from(
            new ClinicalTrialWithSubmissions(clinicalTrial, List.of(submission)));

    assertThat(response.submissions(), hasSize(1));
    assertThat(response.submissions().getFirst().status(), is(SubmissionStatus.SUBMITTED));
  }

  private ClinicalTrialWithSubmissions reported(ClinicalTrial clinicalTrial) {
    return new ClinicalTrialWithSubmissions(clinicalTrial, List.of());
  }

  private URI clinicalTrialId(String projectIdentifier) {
    return UriWrapper.fromUri(projectId(projectIdentifier)).addChild(CLINICAL_TRIAL_PATH).getUri();
  }
}
