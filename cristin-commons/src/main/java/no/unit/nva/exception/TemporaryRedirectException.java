package no.unit.nva.exception;

import java.net.URI;
import nva.commons.apigateway.exceptions.RedirectException;

public class TemporaryRedirectException extends RedirectException {

  public static final int TEMPORARY_REDIRECT = 307;

  private static final String ERROR_MESSAGE_TEMPORARY_REDIRECT =
      "The requested resource is temporarily available at '%s'";

  private final URI location;

  public TemporaryRedirectException(URI location) {
    super(ERROR_MESSAGE_TEMPORARY_REDIRECT.formatted(location));
    this.location = location;
  }

  @Override
  public URI getLocation() {
    return location;
  }

  @Override
  protected Integer statusCode() {
    return TEMPORARY_REDIRECT;
  }
}
