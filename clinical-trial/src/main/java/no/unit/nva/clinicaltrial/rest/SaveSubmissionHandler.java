package no.unit.nva.clinicaltrial.rest;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.getPeriodIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.getProjectIdentifier;
import static no.unit.nva.clinicaltrial.rest.RequestUtils.userFrom;
import static nva.commons.core.attempt.Try.attempt;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.Inclusion;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.write.SubmissionWrite;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.clinicaltrial.service.SubmissionService;
import no.unit.nva.identifiers.OrganizationIdentifier;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ConflictException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;

public class SaveSubmissionHandler extends ApiGatewayHandler<SubmissionRequest, Void> {

  private static final String SUBMISSION_DOES_NOT_EXIST = "Submission does not exist";
  private static final String PERIOD_ALREADY_HAS_SUBMISSION =
      "Period already has a submission for this clinical trial";

  private final transient SubmissionService service;

  public SaveSubmissionHandler(SubmissionService service, Environment environment) {
    super(SubmissionRequest.class, environment);
    this.service = service;
  }

  @Override
  protected void validateRequest(SubmissionRequest input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    // no-op
  }

  @Override
  protected Void processInput(SubmissionRequest input, RequestInfo requestInfo, Context context)
      throws ApiGatewayException {
    save(new SubmissionWrite(createSubmission(input, requestInfo), userFrom(requestInfo)));
    return null;
  }

  private Submission createSubmission(SubmissionRequest input, RequestInfo requestInfo)
      throws ApiGatewayException {
    return new Submission(
        getProjectIdentifier(requestInfo),
        getPeriodIdentifier(requestInfo),
        inclusionsOf(input),
        null,
        null);
  }

  private static List<Inclusion> inclusionsOf(SubmissionRequest input) {
    return Optional.ofNullable(input)
        .map(SubmissionRequest::inclusions)
        .orElseGet(List::of)
        .stream()
        .map(SaveSubmissionHandler::inclusionOf)
        .toList();
  }

  private static Inclusion inclusionOf(InclusionRequest inclusion) {
    return Objects.isNull(inclusion)
        ? new Inclusion(null, null)
        : new Inclusion(institutionIdentifierOf(inclusion.institution()), inclusion.count());
  }

  private static OrganizationIdentifier institutionIdentifierOf(URI institution) {
    return attempt(() -> OrganizationIdentifier.fromUri(institution)).orElse(_ -> null);
  }

  @Override
  protected Integer getSuccessStatusCode(SubmissionRequest input, Void output) {
    return HTTP_CREATED;
  }

  private void save(SubmissionWrite submissionWrite) throws ApiGatewayException {
    try {
      service.save(submissionWrite);
    } catch (ConditionFailedException conditionFailed) {
      throw new ConflictException(PERIOD_ALREADY_HAS_SUBMISSION);
    } catch (ItemNotFoundException notFound) {
      throw new NotFoundException(SUBMISSION_DOES_NOT_EXIST);
    }
  }
}
