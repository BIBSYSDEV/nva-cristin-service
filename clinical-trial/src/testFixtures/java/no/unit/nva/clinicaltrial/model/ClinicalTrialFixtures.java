package no.unit.nva.clinicaltrial.model;

import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
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
import no.unit.nva.model.ApprovalStatus;

public final class ClinicalTrialFixtures {

  private static final String CUSTOMER_PATH = "customer";
  private static final String PROJECT_MANAGER_ROLE = "ProjectManager";

  private ClinicalTrialFixtures() {}

  public static URI randomCustomer() {
    return getNvaApiId(UUID.randomUUID().toString(), CUSTOMER_PATH);
  }

  public static URI personId(String personIdentifier) {
    return getNvaApiId(personIdentifier, PERSON_PATH_NVA);
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
    return new User(randomString(), randomPersonIdentifier(), UUID.randomUUID(), grants);
  }

  public static NvaProject projectManagedBy(String personIdentifier) {
    return projectWithManager(personIdentifier).build();
  }

  public static String randomInstitutionIdentifier() {
    return randomInteger() + ".0.0.0";
  }

  public static NvaProject eligibleProjectManagedBy(String personIdentifier) {
    return projectWithManager(personIdentifier)
        .withHealthProjectData(new HealthProjectData(HealthProjectType.DRUGSTUDY, null, null))
        .withApprovals(List.of(new Approval(null, null, ApprovalStatus.APPROVED, null, null, null)))
        .build();
  }

  private static NvaProject.Builder projectWithManager(String personIdentifier) {
    return new NvaProject.Builder()
        .withContributors(
            List.of(
                new NvaContributor(
                    manager(personIdentifier), List.of(new Role(PROJECT_MANAGER_ROLE, null)))));
  }

  private static Person manager(String personIdentifier) {
    return new Person(
        Optional.ofNullable(personIdentifier).map(ClinicalTrialFixtures::personId).orElse(null),
        randomString(),
        randomString(),
        randomString(),
        randomString());
  }

  public static String randomPersonIdentifier() {
    return randomInteger().toString();
  }

  public static String randomPeriodIdentifier() {
    return randomString();
  }

  private static Inclusion randomInclusion() {
    return new Inclusion(randomString(), randomInteger());
  }

  public static String randomProjectIdentifier() {
    return randomInteger().toString();
  }
}
