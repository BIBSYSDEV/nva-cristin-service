package no.unit.nva.utils;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.RequestInfo;
import org.junit.jupiter.api.Test;

class LogUtilsTest {

  private static final String CLIENT_ID = "some-client-id";
  private static final String NOT_PRESENT = "not present";

  @Test
  void shouldExtractClientIdFromToken() throws Exception {
    var requestInfo =
        requestInfo(new HandlerRequestBuilder<Void>(dtoObjectMapper).withClientId(CLIENT_ID));

    assertThat(LogUtils.extractClientId(requestInfo)).isEqualTo(CLIENT_ID);
  }

  @Test
  void shouldReturnNotPresentWhenTokenHasNoClientId() throws Exception {
    var requestInfo = requestInfo(new HandlerRequestBuilder<Void>(dtoObjectMapper));

    assertThat(LogUtils.extractClientId(requestInfo)).isEqualTo(NOT_PRESENT);
  }

  private static RequestInfo requestInfo(HandlerRequestBuilder<Void> builder) throws Exception {
    return RequestInfo.fromRequest(builder.build());
  }
}
