package no.unit.nva.clinicaltrial.service;

import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.cristin.projects.fetch.FetchCristinProjectApiClient;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.core.JacocoGenerated;

public class ClinicalTrialService {

  private static final String PROJECT_NOT_ELIGIBLE = "Project is not eligible for a clinical trial";

  private final transient FetchApiClient<String, NvaProject> projectsApi;
  private final transient ClinicalTrialRepository repository;

  public ClinicalTrialService(
      FetchApiClient<String, NvaProject> projectsApi, ClinicalTrialRepository repository) {
    this.projectsApi = projectsApi;
    this.repository = repository;
  }

  @JacocoGenerated
  public static ClinicalTrialService defaultService() {
    return new ClinicalTrialService(
        new FetchCristinProjectApiClient()::queryOneCristinProjectUsingIdIntoNvaProject,
        ClinicalTrialRepository.defaultRepository());
  }

  public void save(ClinicalTrial clinicalTrial, User user)
      throws ApiGatewayException, ConditionFailedException, ItemNotFoundException {
    var project = projectsApi.executeFetch(clinicalTrial.projectIdentifier());
    var writer = ProjectAccess.granted(project, user);
    verifyEligible(project);
    repository.save(writer, clinicalTrial);
  }

  private static void verifyEligible(NvaProject project) throws ConflictException {
    if (!project.isClinicalTrialEligible()) {
      throw new ConflictException(PROJECT_NOT_ELIGIBLE);
    }
  }
}
