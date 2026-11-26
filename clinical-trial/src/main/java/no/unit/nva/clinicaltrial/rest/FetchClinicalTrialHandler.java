package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.rest.RequestUtils.getProjectIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.userFrom;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import no.unit.nva.clinicaltrial.service.ClinicalTrialService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class FetchClinicalTrialHandler extends ApiGatewayHandler<Void, ClinicalTrialResponse> {

  private final transient ClinicalTrialService service;

  public FetchClinicalTrialHandler(ClinicalTrialService service, Environment environment) {
    super(Void.class, environment);
    this.service = service;
  }

  @Override
  protected void validateRequest(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    // no-op
  }

  @Override
  protected ClinicalTrialResponse processInput(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var reported = service.find(getProjectIdentifier(requestInfo), userFrom(requestInfo));
    addAdditionalHeaders(() ->
        Map.of(
            EntityTag.ETAG_HEADER,
            EntityTag.fromVersion(reported.clinicalTrial().version()).headerValue()));
    return ClinicalTrialResponse.from(reported);
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, ClinicalTrialResponse output) {
    return HttpURLConnection.HTTP_OK;
  }
}
