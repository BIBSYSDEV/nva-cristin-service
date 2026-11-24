package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.clinicaltrial.rest.EntityTag.VERSION_DOES_NOT_MATCH;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.getProjectIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.userFrom;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.ClinicalTrialWrite;
import no.unit.nva.clinicaltrial.persistence.ClinicalTrialNotFoundException;
import no.unit.nva.clinicaltrial.persistence.ConditionFailedException;
import no.unit.nva.clinicaltrial.service.ClinicalTrialService;
import no.unit.nva.identifiers.SortableIdentifier;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.apigateway.exceptions.PreconditionFailedException;
import nva.commons.core.Environment;

public class SaveClinicalTrialHandler extends ApiGatewayHandler<Void, ClinicalTrialResponse> {

  private static final String ETAG_HEADER = "ETag";
  private static final String PROJECT_ALREADY_HAS_CLINICAL_TRIAL =
      "Project already has a clinical trial";

  private final transient ClinicalTrialService service;
  private transient ClinicalTrialOperation operation;

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
    operation = ClinicalTrialOperation.fromRequest(requestInfo);
    var clinicalTrial = createClinicalTrial(requestInfo);
    var user = userFrom(requestInfo);
    var persisted = save(new ClinicalTrialWrite(clinicalTrial, user));
    addAdditionalHeaders(
        () -> Map.of(ETAG_HEADER, EntityTag.fromVersion(persisted.version()).headerValue()));
    return ClinicalTrialResponse.from(persisted);
  }

  private ClinicalTrial createClinicalTrial(RequestInfo requestInfo) throws ApiGatewayException {
    return new ClinicalTrial(getProjectIdentifier(requestInfo), version(requestInfo));
  }

  @Override
  protected Integer getSuccessStatusCode(Void input, ClinicalTrialResponse output) {
    return operation.isCreate() ? HTTP_CREATED : HTTP_OK;
  }

  private ClinicalTrial save(ClinicalTrialWrite clinicalTrialWrite) throws ApiGatewayException {
    try {
      return service.save(clinicalTrialWrite);
    } catch (ConditionFailedException conditionFailed) {
      throw operation.isCreate()
          ? new ConflictException(PROJECT_ALREADY_HAS_CLINICAL_TRIAL)
          : new PreconditionFailedException(VERSION_DOES_NOT_MATCH);
    } catch (ClinicalTrialNotFoundException notFound) {
      throw new NotFoundException(notFound.getMessage());
    }
  }

  private SortableIdentifier version(RequestInfo requestInfo) throws ApiGatewayException {
    return operation.isCreate() ? null : EntityTag.fromRequest(requestInfo).version();
  }
}
