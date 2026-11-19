package no.unit.nva.clinicaltrial.model;

import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;

import java.util.UUID;

public final class ClinicalTrialFixtures {

  private ClinicalTrialFixtures() {}

  public static ClinicalTrial randomClinicalTrial() {
    return new ClinicalTrial(randomProjectIdentifier());
  }

  public static User randomUser() {
    return new User(randomString(), UUID.randomUUID());
  }

  public static String randomProjectIdentifier() {
    return randomInteger().toString();
  }
}
