package no.unit.nva.clinicaltrial.service;

import static java.util.stream.Collectors.toSet;
import static no.unit.nva.clinicaltrial.model.Grant.APPLICATION_ADMIN;
import static no.unit.nva.clinicaltrial.model.Grant.CLINICAL_TRIAL_ADMIN;
import static no.unit.nva.clinicaltrial.model.Grant.PROJECT_MANAGER;
import static no.unit.nva.cristin.projects.update.UpdateProjectResourceAccessCheck.PROJECT_MANAGER_ROLE_CODE;
import static nva.commons.core.attempt.Try.attempt;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.identifiers.PersonIdentifier;
import nva.commons.apigateway.exceptions.ForbiddenException;

public final class ProjectAccess {

  private static final Set<Grant> WRITERS = Set.of(PROJECT_MANAGER, CLINICAL_TRIAL_ADMIN);
  private static final Set<Grant> READERS =
      Set.of(PROJECT_MANAGER, CLINICAL_TRIAL_ADMIN, APPLICATION_ADMIN);

  private ProjectAccess() {}

  public static User granted(NvaProject project, User user) throws ForbiddenException {
    return caller(project, user, WRITERS);
  }

  public static User reader(NvaProject project, User user) throws ForbiddenException {
    return caller(project, user, READERS);
  }

  private static User caller(NvaProject project, User user, Set<Grant> allowed)
      throws ForbiddenException {
    var caller = isProjectManager(project, user) ? user.withGrant(PROJECT_MANAGER) : user;
    if (allowed.stream().noneMatch(caller.grants()::contains)) {
      throw new ForbiddenException();
    }
    return caller;
  }

  private static boolean isProjectManager(NvaProject project, User user) {
    return Objects.nonNull(user.personIdentifier())
        && projectManagers(project).contains(user.personIdentifier());
  }

  private static Set<PersonIdentifier> projectManagers(NvaProject project) {
    return contributors(project).stream()
        .filter(Objects::nonNull)
        .filter(ProjectAccess::hasProjectManagerRole)
        .map(NvaContributor::identity)
        .filter(Objects::nonNull)
        .map(Person::getId)
        .map(ProjectAccess::personIdentifierOf)
        .flatMap(Optional::stream)
        .collect(toSet());
  }

  private static Optional<PersonIdentifier> personIdentifierOf(URI personId) {
    return attempt(() -> PersonIdentifier.fromUri(personId)).toOptional();
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
