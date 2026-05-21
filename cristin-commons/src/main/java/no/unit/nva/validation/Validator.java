package no.unit.nva.validation;

import nva.commons.apigateway.exceptions.ApiGatewayException;

@FunctionalInterface
public interface Validator<T> {

  void validate(T classOfT) throws ApiGatewayException;
}
