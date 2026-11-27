package no.unit.nva.clinicaltrial.service;

import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.applicationAdmin;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.clinicalTrialAdmin;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.projectManagedBy;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomPersonIdentifier;
import static no.unit.nva.clinicaltrial.model.ClinicalTrialFixtures.randomUser;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.UUID;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.identifiers.CustomerIdentifier;
import nva.commons.apigateway.exceptions.ForbiddenException;
import org.junit.jupiter.api.Test;

class ProjectAccessTest {

  @Test
  void shouldGrantProjectManagerWhenCallerManagesProject() throws Exception {
    var user = randomUser();

    var granted = ProjectAccess.granted(projectManagedBy(user.personIdentifier()), user);

    assertThat(granted.grants(), is(Set.of(Grant.PROJECT_MANAGER)));
  }

  @Test
  void shouldKeepAdminGrantWhenCallerDoesNotManageProject() throws Exception {
    var admin = clinicalTrialAdmin();

    var granted = ProjectAccess.granted(projectManagedBy(randomPersonIdentifier()), admin);

    assertThat(granted.grants(), is(Set.of(Grant.CLINICAL_TRIAL_ADMIN)));
  }

  @Test
  void shouldLetApplicationAdminReadProjectTheyDoNotManage() throws Exception {
    var admin = applicationAdmin();

    var reader = ProjectAccess.reader(projectManagedBy(randomPersonIdentifier()), admin);

    assertThat(reader.grants(), is(Set.of(Grant.APPLICATION_ADMIN)));
  }

  @Test
  void shouldRefuseWriteFromApplicationAdmin() {
    var project = projectManagedBy(randomPersonIdentifier());

    assertThrows(
        ForbiddenException.class, () -> ProjectAccess.granted(project, applicationAdmin()));
  }

  @Test
  void shouldThrowForbiddenWhenCallerNeitherManagesProjectNorIsAdmin() {
    var project = projectManagedBy(randomPersonIdentifier());

    assertThrows(ForbiddenException.class, () -> ProjectAccess.granted(project, randomUser()));
  }

  @Test
  void shouldThrowForbiddenWhenProjectManagerIsUnidentifiedAndCallerHasNoPersonIdentifier() {
    var project = projectManagedBy(null);
    var caller =
        new User(randomString(), null, new CustomerIdentifier(UUID.randomUUID()), Set.of());

    assertThrows(ForbiddenException.class, () -> ProjectAccess.granted(project, caller));
  }
}
