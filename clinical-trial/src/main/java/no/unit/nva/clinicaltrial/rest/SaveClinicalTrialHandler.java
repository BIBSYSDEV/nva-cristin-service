package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.clinicaltrial.rest.EntityTag.VERSION_DOES_NOT_MATCH;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.getProjectIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.userFrom;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import no.unit.nva.clinicaltrial.model.write.User;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.clinicaltrial.service.ClinicalTrialService;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.apigateway.exceptions.PreconditionFailedException;
import nva.commons.core.Environment;

public class SaveClinicalTrialHandler extends ApiGatewayHandler<ClinicalTrialRequest, Void> {

  public static final String INVALID_REPORTABLE = "Reportable must be Yes or No";
  private static final String CLINICAL_TRIAL_DOES_NOT_EXIST = "Clinical trial does not exist";
  private static final String PROJECT_ALREADY_HAS_CLINICAL_TRIAL =
      "Project already has a clinical trial";

  private final transient ClinicalTrialService service;
  private transient ClinicalTrialOperation operation;

  public SaveClinicalTrialHandler(ClinicalTrialService service, Environment environment) {
    super(ClinicalTrialRequest.class, environment);
    this.service = service;
  }

  @Override
  protected void validateRequest(
      ClinicalTrialRequest input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    // no-op
  }

  @Override
  protected Void processInput(ClinicalTrialRequest input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    operation = ClinicalTrialOperation.fromRequest(requestInfo);
    var clinicalTrial = createClinicalTrial(input, requestInfo);
    save(clinicalTrial, userFrom(requestInfo));
    return null;
  }

  private ClinicalTrial createClinicalTrial(ClinicalTrialRequest input, RequestInfo requestInfo)
      throws ApiGatewayException {
    return new ClinicalTrial(
        getProjectIdentifier(requestInfo), reportableOf(input), version(requestInfo));
  }

  private static Reportable reportableOf(ClinicalTrialRequest input) throws BadRequestException {
    return Optional.ofNullable(input)
        .map(ClinicalTrialRequest::reportable)
        .orElseThrow(() -> new BadRequestException(INVALID_REPORTABLE));
  }

  @Override
  protected Integer getSuccessStatusCode(ClinicalTrialRequest input, Void output) {
    return operation.isCreate() ? HTTP_CREATED : HTTP_NO_CONTENT;
  }

  private void save(ClinicalTrial clinicalTrial, User user) throws ApiGatewayException {
    try {
      service.save(clinicalTrial, user);
    } catch (ConditionFailedException conditionFailed) {
      throw operation.isCreate()
          ? new ConflictException(PROJECT_ALREADY_HAS_CLINICAL_TRIAL)
          : new PreconditionFailedException(VERSION_DOES_NOT_MATCH);
    } catch (ItemNotFoundException notFound) {
      throw new NotFoundException(CLINICAL_TRIAL_DOES_NOT_EXIST);
    }
  }

  private Long version(RequestInfo requestInfo) throws ApiGatewayException {
    return operation.isCreate() ? null : EntityTag.fromRequest(requestInfo).version();
  }
}
