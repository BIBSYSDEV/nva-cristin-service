package no.unit.nva.client;

import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class HttpClientProviderTest {

  @Test
  void shouldReuseSameHttpClientAcrossCalls() {
    assertSame(defaultHttpClient(), defaultHttpClient());
  }
}
