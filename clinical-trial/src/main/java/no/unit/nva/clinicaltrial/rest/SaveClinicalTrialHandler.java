package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.getProjectIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.userFrom;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.persistence.ConditionFailedException;
import no.unit.nva.clinicaltrial.service.ClinicalTrialService;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.core.Environment;

public class SaveClinicalTrialHandler extends ApiGatewayHandler<Void, ClinicalTrialResponse> {

  private static final String ETAG_HEADER = "ETag";
  private static final String ETAG_FORMAT = "\"%s\"";
  private static final String PROJECT_ALREADY_HAS_CLINICAL_TRIAL =
      "Project already has a clinical trial";
  private static final SortableIdentifier UNVERSIONED = null;

  private final transient ClinicalTrialService service;

  public SaveClinicalTrialHandler(ClinicalTrialService service, Environment environment) {
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
    var clinicalTrial = new ClinicalTrial(getProjectIdentifier(requestInfo), UNVERSIONED);
    var persisted = save(new ClinicalTrialWrite(clinicalTrial, userFrom(requestInfo)));
    addAdditionalHeaders(() -> Map.of(ETAG_HEADER, ETAG_FORMAT.formatted(persisted.version())));
    return ClinicalTrialResponse.from(persisted);
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, ClinicalTrialResponse output) {
    return HTTP_CREATED;
  }

  private ClinicalTrial save(ClinicalTrialWrite clinicalTrialWrite) throws ApiGatewayException {
    try {
      return service.save(clinicalTrialWrite);
    } catch (ConditionFailedException conditionFailed) {
      throw new ConflictException(PROJECT_ALREADY_HAS_CLINICAL_TRIAL);
    }
  }
}
