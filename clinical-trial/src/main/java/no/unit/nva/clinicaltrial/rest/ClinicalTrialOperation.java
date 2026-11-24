package no.unit.nva.clinicaltrial.rest;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Optional;
import nva.commons.apigateway.RequestInfo;

public enum ClinicalTrialOperation {
  CREATE("CreateClinicalTrial"),
  UPDATE("UpdateClinicalTrial");

  private static final String OPERATION_NAME = "operationName";
  private static final String UNSUPPORTED_OPERATION =
      "Request is not mapped to a known clinical trial operation";

  private final String operationId;

  ClinicalTrialOperation(String operationId) {
    this.operationId = operationId;
  }

  public static ClinicalTrialOperation fromRequest(RequestInfo requestInfo) {
    return Optional.ofNullable(requestInfo.getRequestContext())
        .map(requestContext -> requestContext.get(OPERATION_NAME))
        .map(JsonNode::asText)
        .flatMap(ClinicalTrialOperation::fromOperationId)
        .orElseThrow(() -> new IllegalStateException(UNSUPPORTED_OPERATION));
  }

  public boolean isCreate() {
    return this == CREATE;
  }

  private static Optional<ClinicalTrialOperation> fromOperationId(String operationId) {
    return Arrays.stream(values())
        .filter(operation -> operation.operationId.equals(operationId))
        .findAny();
  }
}
