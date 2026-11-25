package no.unit.nva.clinicaltrial.model;

import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.cristin.projects.model.nva.Approval;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.HealthProjectType;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.identifiers.CustomerIdentifier;
import no.unit.nva.identifiers.OrganizationIdentifier;
import no.unit.nva.identifiers.PersonIdentifier;
import no.unit.nva.model.ApprovalStatus;

public final class ClinicalTrialFixtures {

  private static final String PROJECT_MANAGER_ROLE = "ProjectManager";

  private ClinicalTrialFixtures() {}

  public static URI randomCustomer() {
    return new CustomerIdentifier(UUID.randomUUID()).toId(DOMAIN_NAME);
  }

  public static URI personId(PersonIdentifier personIdentifier) {
    return personIdentifier.toId(DOMAIN_NAME);
  }

  public static ClinicalTrial clinicalTrial(Long version) {
    return clinicalTrial(Reportable.YES, version);
  }

  public static ClinicalTrial clinicalTrial(Reportable reportable, Long version) {
    return new ClinicalTrial(randomProjectIdentifier(), reportable, version);
  }

  public static Submission submission(Long version) {
    return new Submission(
        randomProjectIdentifier(),
        randomPeriodIdentifier(),
        List.of(randomInclusion()),
        SubmissionStatus.SUBMITTED,
        version);
  }

  public static User randomUser() {
    return user(Set.of());
  }

  public static User clinicalTrialAdmin() {
    return user(Set.of(Grant.CLINICAL_TRIAL_ADMIN));
  }

  public static User user(Set<Grant> grants) {
    return new User(
        randomString(),
        randomPersonIdentifier(),
        new CustomerIdentifier(UUID.randomUUID()),
        grants);
  }

  public static NvaProject projectManagedBy(PersonIdentifier personIdentifier) {
    return projectWithManager(personIdentifier).build();
  }

  public static URI organizationId(OrganizationIdentifier institutionIdentifier) {
    return getNvaApiId(institutionIdentifier.value(), ORGANIZATION_PATH);
  }

  public static OrganizationIdentifier randomInstitutionIdentifier() {
    return new OrganizationIdentifier(randomInteger() + ".0.0.0");
  }

  public static NvaProject eligibleProjectManagedBy(PersonIdentifier personIdentifier) {
    return projectWithManager(personIdentifier)
        .withHealthProjectData(new HealthProjectData(HealthProjectType.DRUGSTUDY, null, null))
        .withApprovals(List.of(new Approval(null, null, ApprovalStatus.APPROVED, null, null, null)))
        .build();
  }

  private static NvaProject.Builder projectWithManager(PersonIdentifier personIdentifier) {
    return new NvaProject.Builder()
        .withContributors(
            List.of(
                new NvaContributor(
                    manager(personIdentifier), List.of(new Role(PROJECT_MANAGER_ROLE, null)))));
  }

  private static Person manager(PersonIdentifier personIdentifier) {
    return new Person(
        Optional.ofNullable(personIdentifier).map(ClinicalTrialFixtures::personId).orElse(null),
        randomString(),
        randomString(),
        randomString(),
        randomString());
  }

  public static PersonIdentifier randomPersonIdentifier() {
    return new PersonIdentifier(randomInteger().toString());
  }

  public static String randomPeriodIdentifier() {
    return randomString();
  }

  private static Inclusion randomInclusion() {
    return new Inclusion(randomInstitutionIdentifier(), randomInteger());
  }

  public static String randomProjectIdentifier() {
    return randomInteger().toString();
  }
}
