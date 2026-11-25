package no.unit.nva.clinicaltrial.service;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrialAdmin;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.User;
import no.unit.nva.clinicaltrial.persistence.v3.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.v3.LocalDynamoDbTable;
import no.unit.nva.cristin.projects.model.nva.Approval;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.HealthProjectType;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.model.ApprovalStatus;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClinicalTrialServiceTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String PROJECT_MANAGER = "ProjectManager";

  private ClinicalTrialRepository repository;

  @BeforeEach
  void setUp() {
    repository = LocalDynamoDbTable.repositoryOf(LocalDynamoDbTable.create(TABLE_NAME), TABLE_NAME);
  }

  @Test
  void shouldPersistClinicalTrialWhenCallerIsProjectManager() throws Exception {
    var user = randomUser();
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(user.personIdentifier());

    var persisted = service.save(new ClinicalTrialWrite(clinicalTrial, user));

    assertThat(
        repository.find(clinicalTrial.projectIdentifier()).orElseThrow().version(),
        is(persisted.version()));
  }

  @Test
  void shouldRecordProjectManagerAsGrantWhenProjectManagerPersistsClinicalTrial() throws Exception {
    var user = randomUser();
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(user.personIdentifier());

    service.save(new ClinicalTrialWrite(clinicalTrial, user));

    var version = repository.findVersions(clinicalTrial.projectIdentifier()).getFirst();
    assertThat(version.writer(), is(user.withGrant(Grant.PROJECT_MANAGER)));
  }

  @Test
  void shouldRecordKbsAdminAsGrantWhenAdminPersistsClinicalTrialForProjectTheyDoNotManage()
      throws Exception {
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(randomPersonIdentifier());

    service.save(new ClinicalTrialWrite(clinicalTrial, clinicalTrialAdmin()));

    var version = repository.findVersions(clinicalTrial.projectIdentifier()).getFirst();
    assertThat(((User) version.writer()).grants(), is(Set.of(Grant.CLINICAL_TRIAL_ADMIN)));
  }

  @Test
  void shouldPersistClinicalTrialWhenCallerIsKbsAdmin() throws Exception {
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(randomPersonIdentifier());

    var persisted = service.save(new ClinicalTrialWrite(clinicalTrial, clinicalTrialAdmin()));

    assertThat(
        repository.find(clinicalTrial.projectIdentifier()).orElseThrow().version(),
        is(persisted.version()));
  }

  @Test
  void shouldThrowForbiddenWhenCallerIsNotProjectManager() {
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(randomPersonIdentifier());

    assertThrows(
        ForbiddenException.class,
        () -> service.save(new ClinicalTrialWrite(clinicalTrial, randomUser())));
    assertThat(repository.find(clinicalTrial.projectIdentifier()), is(Optional.empty()));
  }

  @Test
  void shouldThrowConflictWhenProjectIsNotEligibleForClinicalTrial() {
    var user = randomUser();
    var service =
        new ClinicalTrialService(
            _ -> projectWithManager(user.personIdentifier()).build(), repository);

    assertThrows(
        ConflictException.class,
        () -> service.save(new ClinicalTrialWrite(clinicalTrial(null), user)));
  }

  @Test
  void shouldThrowNotFoundWhenProjectDoesNotExist() {
    var service =
        new ClinicalTrialService(
            _ -> {
              throw new NotFoundException(randomString());
            },
            repository);

    assertThrows(
        NotFoundException.class,
        () -> service.save(new ClinicalTrialWrite(clinicalTrial(null), randomUser())));
  }

  @Test
  void shouldThrowForbiddenWhenProjectManagerIsUnidentifiedAndCallerHasNoPersonIdentifier() {
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(null);
    var caller = new User(randomString(), null, UUID.randomUUID(), Set.of());

    assertThrows(
        ForbiddenException.class,
        () -> service.save(new ClinicalTrialWrite(clinicalTrial, caller)));
  }

  private ClinicalTrialService serviceReturningProjectManagedBy(String personIdentifier) {
    FetchApiClient<String, NvaProject> projectsApi = _ -> projectManagedBy(personIdentifier);
    return new ClinicalTrialService(projectsApi, repository);
  }

  private NvaProject projectManagedBy(String personIdentifier) {
    return eligible(projectWithManager(personIdentifier)).build();
  }

  private NvaProject.Builder eligible(NvaProject.Builder project) {
    return project
        .withHealthProjectData(new HealthProjectData(HealthProjectType.DRUGSTUDY, null, null))
        .withApprovals(
            List.of(new Approval(null, null, ApprovalStatus.APPROVED, null, null, null)));
  }

  private NvaProject.Builder projectWithManager(String personIdentifier) {
    return new NvaProject.Builder()
        .withContributors(
            List.of(
                new NvaContributor(
                    person(personIdentifier), List.of(new Role(PROJECT_MANAGER, null)))));
  }

  private Person person(String personIdentifier) {
    return new Person(
        Optional.ofNullable(personIdentifier).map(ClinicalTrialFixtures::personId).orElse(null),
        randomString(),
        randomString(),
        randomString(),
        randomString());
  }
}
