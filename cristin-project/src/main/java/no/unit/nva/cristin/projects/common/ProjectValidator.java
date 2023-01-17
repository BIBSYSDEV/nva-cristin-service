package no.unit.nva.cristin.projects.common;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface ProjectValidator {

    <T> void validate(T classOfT) throws ApiGatewayException;

}
