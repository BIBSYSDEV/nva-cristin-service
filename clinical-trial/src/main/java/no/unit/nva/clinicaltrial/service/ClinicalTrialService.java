package no.unit.nva.clinicaltrial.service;

import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWithSubmissions;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;

public class ClinicalTrialService {

  private static final String CLINICAL_TRIAL_DOES_NOT_EXIST = "Clinical trial does not exist";
  private static final String PROJECT_NOT_ELIGIBLE = "Project is not eligible for a clinical trial";

  private final transient FetchApiClient<String, NvaProject> projectsApi;
  private final transient ClinicalTrialRepository repository;

  public ClinicalTrialService(
      FetchApiClient<String, NvaProject> projectsApi, ClinicalTrialRepository repository) {
    this.projectsApi = projectsApi;
    this.repository = repository;
  }

  public void save(ClinicalTrial clinicalTrial, User user)
      throws ApiGatewayException, ConditionFailedException, ItemNotFoundException {
    var project = projectsApi.executeFetch(clinicalTrial.projectIdentifier());
    var writer = ProjectAccess.granted(project, user);
    verifyEligible(project);
    repository.save(writer, clinicalTrial);
  }

  public ClinicalTrialWithSubmissions find(String projectIdentifier, User user)
      throws ApiGatewayException {
    ProjectAccess.reader(projectsApi.executeFetch(projectIdentifier), user);
    return repository
        .findClinicalTrialWithSubmissions(projectIdentifier)
        .orElseThrow(() -> new NotFoundException(CLINICAL_TRIAL_DOES_NOT_EXIST));
  }

  private static void verifyEligible(NvaProject project) throws ConflictException {
    if (!project.isClinicalTrialEligible()) {
      throw new ConflictException(PROJECT_NOT_ELIGIBLE);
    }
  }
}
