package no.unit.nva.clinicaltrial.service;

import static java.util.stream.Collectors.toSet;
import static no.unit.nva.clinicaltrial.model.Grant.PROJECT_MANAGER;
import static no.unit.nva.cristin.projects.update.UpdateProjectResourceAccessCheck.PROJECT_MANAGER_ROLE_CODE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.model.User;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialNotFoundException;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.ConditionFailedException;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.ForbiddenException;

public class ClinicalTrialService {

  private static final String PROJECT_NOT_ELIGIBLE = "Project is not eligible for a clinical trial";

  private final transient FetchApiClient<String, NvaProject> projectsApi;
  private final transient ClinicalTrialRepository repository;

  public ClinicalTrialService(
      FetchApiClient<String, NvaProject> projectsApi, ClinicalTrialRepository repository) {
    this.projectsApi = projectsApi;
    this.repository = repository;
  }

  public ClinicalTrial save(ClinicalTrialWrite clinicalTrialWrite)
      throws ApiGatewayException, ConditionFailedException, ClinicalTrialNotFoundException {
    var project = projectsApi.executeFetch(clinicalTrialWrite.clinicalTrial().projectIdentifier());
    var user = granted(project, clinicalTrialWrite.user());
    if (!user.isGranted()) {
      throw new ForbiddenException();
    }
    verifyEligible(project);
    return repository.save(new ClinicalTrialWrite(clinicalTrialWrite.clinicalTrial(), user));
  }

  private static void verifyEligible(NvaProject project) throws ConflictException {
    if (!project.isClinicalTrialEligible()) {
      throw new ConflictException(PROJECT_NOT_ELIGIBLE);
    }
  }

  private static User granted(NvaProject project, User user) {
    return isProjectManager(project, user) ? user.withGrant(PROJECT_MANAGER) : user;
  }

  private static boolean isProjectManager(NvaProject project, User user) {
    return Objects.nonNull(user.personIdentifier())
        && projectManagers(project).contains(user.personIdentifier());
  }

  private static Set<String> projectManagers(NvaProject project) {
    return contributors(project).stream()
        .filter(Objects::nonNull)
        .filter(ClinicalTrialService::hasProjectManagerRole)
        .map(NvaContributor::identity)
        .filter(Objects::nonNull)
        .map(Person::getId)
        .map(UriUtils::extractLastPathElement)
        .filter(Objects::nonNull)
        .collect(toSet());
  }

  private static List<NvaContributor> contributors(NvaProject project) {
    return Optional.ofNullable(project.getContributors()).orElseGet(List::of);
  }

  private static boolean hasProjectManagerRole(NvaContributor contributor) {
    return roles(contributor).stream()
        .anyMatch(role -> PROJECT_MANAGER_ROLE_CODE.equals(role.type()));
  }

  private static List<Role> roles(NvaContributor contributor) {
    return Optional.ofNullable(contributor.roles()).orElseGet(List::of);
  }
}
