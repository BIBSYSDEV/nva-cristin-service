package no.unit.nva.clinicaltrial.service;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomInstitutionIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPeriodIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomProjectIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;
import no.unit.nva.clinicaltrial.model.write.System;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialRepository;
import no.unit.nva.clinicaltrial.persistence.LocalDynamoDbTable;
import no.unit.nva.identifiers.PersonIdentifier;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubmissionServiceTest {

  private static final String TABLE_NAME = "clinical-trial";
  private static final String ADMIN_PROCESS = "period-administration";

  private ClinicalTrialRepository repository;
  private String periodIdentifier;
  private String projectIdentifier;

  @BeforeEach
  void setUp() throws Exception {
    repository = LocalDynamoDbTable.repositoryOf(LocalDynamoDbTable.create(TABLE_NAME), TABLE_NAME);
    periodIdentifier = randomPeriodIdentifier();
    projectIdentifier = randomProjectIdentifier();
    repository.save(new System(ADMIN_PROCESS), new Period(periodIdentifier, null));
    repository.save(randomUser(), new ClinicalTrial(projectIdentifier, Reportable.YES, null));
  }

  @Test
  void shouldPersistSubmissionAsSubmittedWhenCallerIsProjectManager() throws Exception {
    var user = randomUser();
    var institution = randomInstitutionIdentifier();
    var submission = submissionOf(List.of(new Inclusion(institution, 3)), null);
    var service = serviceFor(user.personIdentifier());

    service.save(submission, user);

    var persisted = persisted(submission);
    assertThat(persisted.status(), is(SubmissionStatus.SUBMITTED));
    assertThat(persisted.inclusions(), is(submission.inclusions()));
  }

  @Test
  void shouldReplaceInclusionsWhenSubmissionIsUpdated() throws Exception {
    var user = randomUser();
    var institution = randomInstitutionIdentifier();
    var service = serviceFor(user.personIdentifier());
    var submission = submissionOf(List.of(new Inclusion(institution, 3)), null);
    service.save(submission, user);

    var replaced = withInclusions(persisted(submission), List.of(new Inclusion(institution, 7)));
    service.save(replaced, user);

    assertThat(persisted(submission).inclusions(), is(replaced.inclusions()));
  }

  @Test
  void shouldNotPersistSubmissionWhenCallerIsNotGrantedAccessToProject() {
    var institution = randomInstitutionIdentifier();
    var submission = submissionOf(List.of(new Inclusion(institution, 3)), null);
    var service = serviceFor(randomPersonIdentifier());

    assertThrows(ForbiddenException.class, () -> service.save(submission, randomUser()));
    assertThat(
        repository.findSubmission(submission.projectIdentifier(), submission.periodIdentifier()),
        is(Optional.empty()));
  }

  @Test
  void shouldReturnSubmissionWhenCallerIsProjectManager() throws Exception {
    var user = randomUser();
    var submission = submissionOf(List.of(new Inclusion(randomInstitutionIdentifier(), 3)), null);
    var service = serviceFor(user.personIdentifier());
    service.save(submission, user);

    var found = service.find(projectIdentifier, periodIdentifier, user);

    assertThat(found.version(), is(1L));
  }

  @Test
  void shouldThrowNotFoundWhenReadingSubmissionOfProjectThatDoesNotExist() {
    var service =
        new SubmissionService(
            _ -> {
              throw new NotFoundException(randomString());
            },
            repository);

    assertThrows(
        NotFoundException.class,
        () -> service.find(projectIdentifier, periodIdentifier, randomUser()));
  }

  @Test
  void shouldThrowNotFoundWhenPeriodHasNoSubmission() {
    var user = randomUser();
    var service = serviceFor(user.personIdentifier());

    assertThrows(
        NotFoundException.class, () -> service.find(projectIdentifier, periodIdentifier, user));
  }

  @Test
  void shouldThrowForbiddenWhenCallerReadsProjectTheyAreNotGrantedAccessTo() {
    var service = serviceFor(randomPersonIdentifier());

    assertThrows(
        ForbiddenException.class,
        () -> service.find(projectIdentifier, periodIdentifier, randomUser()));
  }

  @Test
  void shouldThrowNotFoundWhenProjectHasNoClinicalTrial() {
    var user = randomUser();
    var submission =
        new Submission(
            randomProjectIdentifier(),
            periodIdentifier,
            List.of(new Inclusion(randomInstitutionIdentifier(), 3)),
            null,
            null);
    var service = serviceFor(user.personIdentifier());

    assertThrows(NotFoundException.class, () -> service.save(submission, user));
  }

  @Test
  void shouldThrowNotFoundWhenPeriodDoesNotExist() {
    var user = randomUser();
    var submission =
        new Submission(
            projectIdentifier,
            randomPeriodIdentifier(),
            List.of(new Inclusion(randomInstitutionIdentifier(), 3)),
            null,
            null);
    var service = serviceFor(user.personIdentifier());

    assertThrows(NotFoundException.class, () -> service.save(submission, user));
  }

  @Test
  void shouldThrowBadRequestWhenSubmissionReportsNoInclusions() {
    var user = randomUser();
    var submission = submissionOf(List.of(), null);
    var service = serviceFor(user.personIdentifier());

    assertThrows(BadRequestException.class, () -> service.save(submission, user));
  }

  @Test
  void shouldThrowBadRequestWhenInclusionHasNoInstitution() {
    var user = randomUser();
    var submission = submissionOf(List.of(new Inclusion(null, 3)), null);
    var service = serviceFor(user.personIdentifier());

    assertThrows(BadRequestException.class, () -> service.save(submission, user));
  }

  @Test
  void shouldThrowBadRequestWhenInclusionCountIsNegative() {
    var user = randomUser();
    var institution = randomInstitutionIdentifier();
    var submission = submissionOf(List.of(new Inclusion(institution, -1)), null);
    var service = serviceFor(user.personIdentifier());

    assertThrows(BadRequestException.class, () -> service.save(submission, user));
  }

  @Test
  void shouldThrowBadRequestWhenInstitutionIsIncludedTwice() {
    var user = randomUser();
    var institution = randomInstitutionIdentifier();
    var submission =
        submissionOf(List.of(new Inclusion(institution, 3), new Inclusion(institution, 4)), null);
    var service = serviceFor(user.personIdentifier());

    assertThrows(BadRequestException.class, () -> service.save(submission, user));
  }

  private Submission submissionOf(List<Inclusion> inclusions, Long version) {
    return new Submission(projectIdentifier, periodIdentifier, inclusions, null, version);
  }

  private Submission withInclusions(Submission submission, List<Inclusion> inclusions) {
    return new Submission(
        submission.projectIdentifier(),
        submission.periodIdentifier(),
        inclusions,
        submission.status(),
        submission.version());
  }

  private Submission persisted(Submission submission) {
    return repository
        .findSubmission(submission.projectIdentifier(), submission.periodIdentifier())
        .orElseThrow();
  }

  private SubmissionService serviceFor(PersonIdentifier managerPersonIdentifier) {
    return new SubmissionService(_ -> projectManagedBy(managerPersonIdentifier), repository);
  }
}
