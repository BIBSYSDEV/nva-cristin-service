package no.unit.nva.exception;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class UnauthorizedException extends ApiGatewayException {

  public static final String DEFAULT_MESSAGE = "Unauthorized";

  public UnauthorizedException() {
    super(DEFAULT_MESSAGE);
  }

  @Override
  protected Integer statusCode() {
    return HttpURLConnection.HTTP_UNAUTHORIZED;
  }
}
