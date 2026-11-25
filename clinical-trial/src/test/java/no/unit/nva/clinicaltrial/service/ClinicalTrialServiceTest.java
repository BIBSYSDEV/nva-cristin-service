package no.unit.nva.clinicaltrial.service;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrial;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.eligibleProjectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.write.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.LocalDynamoDbTable;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.identifiers.PersonIdentifier;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClinicalTrialServiceTest {

  private static final String TABLE_NAME = "clinical-trial";

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

    service.save(new ClinicalTrialWrite(clinicalTrial, user));

    assertThat(
        repository.findClinicalTrial(clinicalTrial.projectIdentifier()).orElseThrow().version(),
        is(1L));
  }

  @Test
  void shouldRecordProjectManagerAsGrantWhenProjectManagerPersistsClinicalTrial() throws Exception {
    var user = randomUser();
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(user.personIdentifier());

    service.save(new ClinicalTrialWrite(clinicalTrial, user));

    var version =
        repository.findClinicalTrialVersions(clinicalTrial.projectIdentifier()).getFirst();
    assertThat(version.writer(), is(user.withGrant(Grant.PROJECT_MANAGER)));
  }

  @Test
  void shouldNotPersistClinicalTrialWhenCallerIsNotGrantedAccessToProject() {
    var clinicalTrial = clinicalTrial(null);
    var service = serviceReturningProjectManagedBy(randomPersonIdentifier());

    assertThrows(
        ForbiddenException.class,
        () -> service.save(new ClinicalTrialWrite(clinicalTrial, randomUser())));
    assertThat(
        repository.findClinicalTrial(clinicalTrial.projectIdentifier()), is(Optional.empty()));
  }

  @Test
  void shouldThrowConflictWhenProjectIsNotEligibleForClinicalTrial() {
    var user = randomUser();
    var service =
        new ClinicalTrialService(_ -> projectManagedBy(user.personIdentifier()), repository);

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

  private ClinicalTrialService serviceReturningProjectManagedBy(PersonIdentifier personIdentifier) {
    FetchApiClient<String, NvaProject> projectsApi =
        _ -> eligibleProjectManagedBy(personIdentifier);
    return new ClinicalTrialService(projectsApi, repository);
  }
}
