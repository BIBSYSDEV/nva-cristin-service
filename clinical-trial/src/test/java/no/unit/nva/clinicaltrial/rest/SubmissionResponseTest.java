package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.organizationId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectId;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomInstitutionIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPeriodIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.URI;
import java.util.List;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import no.unit.nva.identifiers.OrganizationIdentifier;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

class SubmissionResponseTest {

  private static final String CLINICAL_TRIAL_PATH = "clinical-trial";

  @Test
  void shouldLinkToItselfAndToItsProject() {
    var submission = submission(randomInstitutionIdentifier());

    var response = SubmissionResponse.from(submission);

    assertThat(response.id(), is(submissionId(submission)));
    assertThat(response.project(), is(projectId(submission.projectIdentifier())));
  }

  @Test
  void shouldCarryPeriodStatusAndProjectContext() {
    var submission = submission(randomInstitutionIdentifier());

    var response = SubmissionResponse.from(submission);

    assertThat(response.period(), is(submission.periodIdentifier()));
    assertThat(response.context(), is(URI.create(PROJECT_LOOKUP_CONTEXT_URL)));
  }

  @Test
  void shouldCarryItsTypeName() {
    var response = SubmissionResponse.from(submission(randomInstitutionIdentifier()));

    assertThat(dtoObjectMapper.valueToTree(response).get("type").asText(), is("Submission"));
  }

  @Test
  void shouldReportEachInclusionWithTheInstitutionUri() {
    var institution = randomInstitutionIdentifier();

    var response = SubmissionResponse.from(submission(institution));

    assertThat(response.inclusions().getFirst().institution(), is(organizationId(institution)));
    assertThat(response.inclusions().getFirst().count(), is(9));
  }

  private Submission submission(OrganizationIdentifier institution) {
    return new Submission(
        randomProjectIdentifier(),
        randomPeriodIdentifier(),
        List.of(new Inclusion(institution, 9)),
        SubmissionStatus.SUBMITTED,
        1L);
  }

  private URI submissionId(Submission submission) {
    return UriWrapper.fromUri(projectId(submission.projectIdentifier()))
        .addChild(CLINICAL_TRIAL_PATH)
        .addChild(submission.periodIdentifier())
        .getUri();
  }
}
