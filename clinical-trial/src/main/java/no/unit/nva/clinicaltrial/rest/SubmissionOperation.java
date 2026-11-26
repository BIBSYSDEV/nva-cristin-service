package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.clinicaltrial.rest.RequestUtils.getOperationName;

import java.util.Arrays;
import java.util.Optional;
import nva.commons.apigateway.RequestInfo;

public enum SubmissionOperation {
  CREATE("CreateSubmission"),
  UPDATE("UpdateSubmission");

  private static final String UNSUPPORTED_OPERATION =
      "Request is not mapped to a known submission operation";

  private final String operationId;

  SubmissionOperation(String operationId) {
    this.operationId = operationId;
  }

  public static SubmissionOperation fromRequest(RequestInfo requestInfo) {
    return getOperationName(requestInfo)
        .flatMap(SubmissionOperation::fromOperationId)
        .orElseThrow(() -> new IllegalStateException(UNSUPPORTED_OPERATION));
  }

  public boolean isCreate() {
    return this == CREATE;
  }

  private static Optional<SubmissionOperation> fromOperationId(String operationId) {
    return Arrays.stream(values())
        .filter(operation -> operation.operationId.equals(operationId))
        .findAny();
  }
}
