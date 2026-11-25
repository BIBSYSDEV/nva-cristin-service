package no.unit.nva.clinicaltrial.model;

import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import no.unit.nva.clinicaltrial.model.write.User;

public final class ClinicalTrialFixtures {

  private static final String CUSTOMER_PATH = "customer";

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
