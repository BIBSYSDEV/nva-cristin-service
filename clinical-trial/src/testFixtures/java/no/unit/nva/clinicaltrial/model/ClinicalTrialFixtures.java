package no.unit.nva.clinicaltrial.model;

import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;

import java.util.Set;
import java.util.UUID;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.apigateway.AccessRight;

public final class ClinicalTrialFixtures {

  private ClinicalTrialFixtures() {}

  public static ClinicalTrial clinicalTrial(SortableIdentifier version) {
    return new ClinicalTrial(randomProjectIdentifier(), version);
  }

  public static User randomUser() {
    return user(Set.of());
  }

  public static User kbsAdmin() {
    return user(Set.of(AccessRight.MANAGE_KBS));
  }

  public static User user(Set<AccessRight> accessRights) {
    return new User(randomString(), randomPersonIdentifier(), UUID.randomUUID(), accessRights);
  }

  public static String randomPersonIdentifier() {
    return randomInteger().toString();
  }

  public static String randomProjectIdentifier() {
    return randomInteger().toString();
  }
}
