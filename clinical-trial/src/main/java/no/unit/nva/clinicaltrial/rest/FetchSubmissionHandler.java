package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.rest.RequestUtils.getPeriodIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.getProjectIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.userFrom;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import no.unit.nva.clinicaltrial.service.SubmissionService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class FetchSubmissionHandler extends ApiGatewayHandler<Void, SubmissionResponse> {

  private final transient SubmissionService service;

  public FetchSubmissionHandler(SubmissionService service, Environment environment) {
    super(Void.class, environment);
    this.service = service;
  }

  @Override
  protected void validateRequest(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    // no-op
  }

  @Override
  protected SubmissionResponse processInput(Void input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    var submission =
        service.find(
            getProjectIdentifier(requestInfo),
            getPeriodIdentifier(requestInfo),
            userFrom(requestInfo));
    addAdditionalHeaders(() ->
        Map.of(EntityTag.ETAG_HEADER, EntityTag.fromVersion(submission.version()).headerValue()));
    return SubmissionResponse.from(submission);
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, SubmissionResponse output) {
    return HttpURLConnection.HTTP_OK;
  }
}
