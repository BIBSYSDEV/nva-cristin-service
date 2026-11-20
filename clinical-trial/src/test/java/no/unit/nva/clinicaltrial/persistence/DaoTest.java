package no.unit.nva.clinicaltrial.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class DaoTest {

  @Test
  void shouldKeepJacksonFailureAsCauseWhenItemCannotBeRead() {
    var item = Map.of("createdDate", AttributeValue.fromS("not an instant"));

    var failure =
        assertThrows(IllegalStateException.class, () -> Dao.fromItem(item, ClinicalTrialDao.class));

    assertThat(failure.getCause(), is(notNullValue()));
  }
}
