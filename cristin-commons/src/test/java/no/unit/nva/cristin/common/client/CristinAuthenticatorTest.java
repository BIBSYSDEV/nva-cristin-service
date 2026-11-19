package no.unit.nva.cristin.common.client;

import static no.unit.nva.cristin.common.client.CristinAuthenticator.getHttpClient;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class CristinAuthenticatorTest {

  @Test
  void shouldReuseSameAuthenticatedHttpClientAcrossCalls() {
    assertSame(getHttpClient(), getHttpClient());
  }
}
