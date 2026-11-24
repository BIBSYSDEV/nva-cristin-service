package no.unit.nva.clinicaltrial.rest;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public class PreconditionRequiredException extends ApiGatewayException {

  private static final int PRECONDITION_REQUIRED = 428;

  public PreconditionRequiredException(String message) {
    super(message);
  }

  @Override
  protected Integer statusCode() {
    return PRECONDITION_REQUIRED;
  }
}
