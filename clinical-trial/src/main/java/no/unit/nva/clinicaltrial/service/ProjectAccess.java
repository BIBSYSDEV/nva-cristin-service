package no.unit.nva.clinicaltrial.service;

import static java.util.stream.Collectors.toSet;
import static no.unit.nva.clinicaltrial.model.Grant.PROJECT_MANAGER;
import static no.unit.nva.cristin.projects.update.UpdateProjectResourceAccessCheck.PROJECT_MANAGER_ROLE_CODE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ForbiddenException;

public final class ProjectAccess {

  private ProjectAccess() {}

  public static User granted(NvaProject project, User user) throws ForbiddenException {
    var caller = isProjectManager(project, user) ? user.withGrant(PROJECT_MANAGER) : user;
    if (!caller.isGranted()) {
      throw new ForbiddenException();
    }
    return caller;
  }

  private static boolean isProjectManager(NvaProject project, User user) {
    return Objects.nonNull(user.personIdentifier())
        && projectManagers(project).contains(user.personIdentifier());
  }

  private static Set<String> projectManagers(NvaProject project) {
    return contributors(project).stream()
        .filter(Objects::nonNull)
        .filter(ProjectAccess::hasProjectManagerRole)
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
