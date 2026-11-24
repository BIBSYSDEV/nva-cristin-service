package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.model.Grant.CLINICAL_TRIAL_ADMIN;
import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import static nva.commons.apigateway.AccessRight.MANAGE_KBS;

import java.util.Set;
import java.util.UUID;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.User;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public final class RequestUtils {

  private RequestUtils() {}

  public static String getProjectIdentifier(RequestInfo requestInfo) throws ApiGatewayException {
    return getValidIdentifier(requestInfo);
  }

  public static User userFrom(RequestInfo requestInfo) throws ApiGatewayException {
    return new User(
        requestInfo.getUserName(),
        UriUtils.extractLastPathElement(requestInfo.getPersonCristinId()),
        UUID.fromString(UriUtils.extractLastPathElement(requestInfo.getCurrentCustomer())),
        grantsOf(requestInfo));
  }

  private static Set<Grant> grantsOf(RequestInfo requestInfo) {
    return requestInfo.userIsAuthorized(MANAGE_KBS) ? Set.of(CLINICAL_TRIAL_ADMIN) : Set.of();
  }
}
