package no.unit.nva.clinicaltrial.rest;

import static nva.commons.core.attempt.Try.attempt;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.PreconditionFailedException;

public record EntityTag(Long version) {

  public static final String VERSION_DOES_NOT_MATCH = "ETag does not match the current version";

  private static final String IF_MATCH_HEADER = "If-Match";
  private static final String IF_MATCH_REQUIRED = "Update requires an If-Match header";
  private static final String QUOTE = "\"";
  private static final String EMPTY_STRING = "";

  public static EntityTag fromRequest(RequestInfo requestInfo) throws ApiGatewayException {
    var ifMatchHeader = getIfMatchHeader(requestInfo);
    return attempt(() -> new EntityTag(Long.valueOf(unquoted(ifMatchHeader))))
        .orElseThrow(_ -> new PreconditionFailedException(VERSION_DOES_NOT_MATCH));
  }

  private static String getIfMatchHeader(RequestInfo requestInfo)
      throws PreconditionRequiredException {
    return requestInfo
        .getHeaderOptional(IF_MATCH_HEADER)
        .orElseThrow(() -> new PreconditionRequiredException(IF_MATCH_REQUIRED));
  }

  private static String unquoted(String value) {
    return value.replace(QUOTE, EMPTY_STRING);
  }
}
