package no.unit.nva.clinicaltrial.service;

import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import no.unit.nva.clinicaltrial.model.write.SubmissionWrite;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.identifiers.OrganizationIdentifier;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;

public class SubmissionService {

  public static final String MISSING_INCLUSIONS = "Submission must report at least one institution";

  private static final String CLINICAL_TRIAL_DOES_NOT_EXIST = "Clinical trial does not exist";
  private static final String PERIOD_DOES_NOT_EXIST = "Reporting period does not exist";
  private static final String MISSING_INSTITUTION =
      "Each inclusion must name an institution by its organization identifier";
  private static final String INVALID_COUNT = "Inclusion count must be zero or greater";
  private static final String INSTITUTION_REPEATED = "Each institution may be included only once";

  private final transient FetchApiClient<String, NvaProject> projectsApi;
  private final transient ClinicalTrialRepository repository;

  public SubmissionService(
      FetchApiClient<String, NvaProject> projectsApi, ClinicalTrialRepository repository) {
    this.projectsApi = projectsApi;
    this.repository = repository;
  }

  public void save(SubmissionWrite submissionWrite)
      throws ApiGatewayException, ConditionFailedException, ItemNotFoundException {
    var submission = submissionWrite.submission();
    var project = projectsApi.executeFetch(submission.projectIdentifier());
    var user = ProjectAccess.granted(project, submissionWrite.user());
    verifyClinicalTrialExists(submission.projectIdentifier());
    verifyPeriodExists(submission.periodIdentifier());
    verifyInclusions(submission.inclusions());
    repository.save(new SubmissionWrite(submitted(submission), user));
  }

  private void verifyClinicalTrialExists(String projectIdentifier) throws NotFoundException {
    if (repository.findClinicalTrial(projectIdentifier).isEmpty()) {
      throw new NotFoundException(CLINICAL_TRIAL_DOES_NOT_EXIST);
    }
  }

  private void verifyPeriodExists(String periodIdentifier) throws NotFoundException {
    if (repository.findPeriod(periodIdentifier).isEmpty()) {
      throw new NotFoundException(PERIOD_DOES_NOT_EXIST);
    }
  }

  private static void verifyInclusions(List<Inclusion> inclusions) throws BadRequestException {
    if (inclusions.isEmpty()) {
      throw new BadRequestException(MISSING_INCLUSIONS);
    }
    if (inclusions.stream().anyMatch(SubmissionService::hasNoInstitution)) {
      throw new BadRequestException(MISSING_INSTITUTION);
    }
    if (inclusions.stream().anyMatch(SubmissionService::hasInvalidCount)) {
      throw new BadRequestException(INVALID_COUNT);
    }
    if (institutions(inclusions).size() != inclusions.size()) {
      throw new BadRequestException(INSTITUTION_REPEATED);
    }
  }

  private static boolean hasNoInstitution(Inclusion inclusion) {
    return Objects.isNull(inclusion.institutionIdentifier());
  }

  private static boolean hasInvalidCount(Inclusion inclusion) {
    return Objects.isNull(inclusion.count()) || inclusion.count() < 0;
  }

  private static Set<OrganizationIdentifier> institutions(List<Inclusion> inclusions) {
    return inclusions.stream().map(Inclusion::institutionIdentifier).collect(toSet());
  }

  private static Submission submitted(Submission submission) {
    return new Submission(
        submission.projectIdentifier(),
        submission.periodIdentifier(),
        submission.inclusions(),
        SubmissionStatus.SUBMITTED,
        submission.version());
  }
}
