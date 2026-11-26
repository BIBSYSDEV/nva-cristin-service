package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.model.Grant.CLINICAL_TRIAL_ADMIN;
import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import static nva.commons.apigateway.AccessRight.MANAGE_KBS;
import static nva.commons.core.attempt.Try.attempt;

import java.util.Set;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.identifiers.CustomerIdentifier;
import no.unit.nva.identifiers.PersonIdentifier;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;

public final class RequestUtils {

  private static final String PERIOD_IDENTIFIER = "periodIdentifier";
  private static final String MISSING_PERIOD_IDENTIFIER =
      "Reporting period is missing from the request path";
  private static final String INVALID_PERSON_IDENTIFIER =
      "Caller must be identified by a Cristin person URI";
  private static final String INVALID_CUSTOMER_IDENTIFIER =
      "Caller must act on behalf of a customer identified by a customer URI";

  private RequestUtils() {}

  public static String getProjectIdentifier(RequestInfo requestInfo) throws ApiGatewayException {
    return getValidIdentifier(requestInfo);
  }

  public static String getPeriodIdentifier(RequestInfo requestInfo) throws BadRequestException {
    return attempt(() -> requestInfo.getPathParameter(PERIOD_IDENTIFIER))
        .orElseThrow(_ -> new BadRequestException(MISSING_PERIOD_IDENTIFIER));
  }

  public static User userFrom(RequestInfo requestInfo) throws ApiGatewayException {
    return new User(
        requestInfo.getUserName(),
        personIdentifierOf(requestInfo),
        customerIdentifierOf(requestInfo),
        grantsOf(requestInfo));
  }

  private static PersonIdentifier personIdentifierOf(RequestInfo requestInfo)
      throws ApiGatewayException {
    var personId = requestInfo.getPersonCristinId();
    return attempt(() -> PersonIdentifier.fromUri(personId))
        .orElseThrow(_ -> new BadRequestException(INVALID_PERSON_IDENTIFIER));
  }

  private static CustomerIdentifier customerIdentifierOf(RequestInfo requestInfo)
      throws ApiGatewayException {
    var customer = requestInfo.getCurrentCustomer();
    return attempt(() -> CustomerIdentifier.fromUri(customer))
        .orElseThrow(_ -> new BadRequestException(INVALID_CUSTOMER_IDENTIFIER));
  }

  private static Set<Grant> grantsOf(RequestInfo requestInfo) {
    return requestInfo.userIsAuthorized(MANAGE_KBS) ? Set.of(CLINICAL_TRIAL_ADMIN) : Set.of();
  }
}
