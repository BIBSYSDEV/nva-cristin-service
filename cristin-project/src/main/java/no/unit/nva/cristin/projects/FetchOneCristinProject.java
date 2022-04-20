package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.common.handler.CristinHandler;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.exception.UnauthorizedException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Set;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.utils.AccessUtils.verifyRequesterCanEditProjects;
import static nva.commons.core.attempt.Try.attempt;

public class FetchOneCristinProject extends CristinHandler<Void, NvaProject> {

    public static final Set<String> VALID_QUERY_PARAMETERS = Set.of(LANGUAGE);

    private final transient CristinApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchOneCristinProject() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchOneCristinProject(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    public FetchOneCristinProject(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected NvaProject processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateThatSuppliedQueryParamsIsSupported(requestInfo);

        String language = getValidLanguage(requestInfo);
        String id = getValidIdentifier(requestInfo);

        try {
            return getTransformedProjectFromCristin(id, language);
        } catch (UnauthorizedException unauthorizedException) {
            verifyRequesterCanEditProjects(requestInfo);
            return authenticatedGetTransformedProjectFromCristin(id, language);
        }
    }

    private void validateThatSuppliedQueryParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, NvaProject output) {
        return HttpURLConnection.HTTP_OK;
    }

    protected static String getValidIdentifier(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Integer.parseInt(requestInfo.getPathParameter(IDENTIFIER)))
                .orElseThrow(failure -> new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER));

        return requestInfo.getPathParameter(IDENTIFIER);
    }

    private NvaProject getTransformedProjectFromCristin(String id, String language) throws ApiGatewayException {
        return cristinApiClient.queryOneCristinProjectUsingIdIntoNvaProject(id, language);
    }

    private NvaProject authenticatedGetTransformedProjectFromCristin(String id, String language)
            throws ApiGatewayException {
        return new CristinApiClient(CristinAuthenticator.getHttpClient())
                .queryOneCristinProjectUsingIdIntoNvaProject(id, language);
    }

}
