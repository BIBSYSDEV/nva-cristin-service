package no.unit.nva.clinicaltrial.model;

import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;

import java.util.Set;
import java.util.UUID;
import no.unit.nva.identifiers.SortableIdentifier;

public final class ClinicalTrialFixtures {

  private ClinicalTrialFixtures() {}

  public static ClinicalTrial clinicalTrial(SortableIdentifier version) {
    return new ClinicalTrial(randomProjectIdentifier(), version);
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

  public static String randomPersonIdentifier() {
    return randomInteger().toString();
  }

  public static String randomProjectIdentifier() {
    return randomInteger().toString();
  }
}
