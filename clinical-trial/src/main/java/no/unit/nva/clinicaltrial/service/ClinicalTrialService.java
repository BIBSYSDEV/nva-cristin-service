package no.unit.nva.clinicaltrial.service;

import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.write.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;

public class ClinicalTrialService {

  private static final String PROJECT_NOT_ELIGIBLE = "Project is not eligible for a clinical trial";

  private final transient FetchApiClient<String, NvaProject> projectsApi;
  private final transient ClinicalTrialRepository repository;

  public ClinicalTrialService(
      FetchApiClient<String, NvaProject> projectsApi, ClinicalTrialRepository repository) {
    this.projectsApi = projectsApi;
    this.repository = repository;
  }

  public void save(ClinicalTrialWrite clinicalTrialWrite)
      throws ApiGatewayException, ConditionFailedException, ItemNotFoundException {
    var project = projectsApi.executeFetch(clinicalTrialWrite.clinicalTrial().projectIdentifier());
    var user = ProjectAccess.granted(project, clinicalTrialWrite.user());
    verifyEligible(project);
    repository.save(new ClinicalTrialWrite(clinicalTrialWrite.clinicalTrial(), user));
  }

  private static void verifyEligible(NvaProject project) throws ConflictException {
    if (!project.isClinicalTrialEligible()) {
      throw new ConflictException(PROJECT_NOT_ELIGIBLE);
    }
  }
}
