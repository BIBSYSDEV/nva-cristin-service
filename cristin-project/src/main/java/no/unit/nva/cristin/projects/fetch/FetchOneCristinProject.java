package no.unit.nva.cristin.projects.fetch;

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

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.utils.AccessUtils.verifyRequesterCanEditProjects;

public class FetchOneCristinProject extends CristinHandler<Void, NvaProject> {

    public static final Set<String> VALID_QUERY_PARAMETERS = Set.of(LANGUAGE);

    private final transient FetchCristinProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchOneCristinProject() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchOneCristinProject(Environment environment) {
        this(new FetchCristinProjectApiClient(), environment);
    }

    public FetchOneCristinProject(FetchCristinProjectApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected NvaProject processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateThatSuppliedQueryParamsIsSupported(requestInfo);

        var language = getValidLanguage(requestInfo);
        var id = getValidIdentifier(requestInfo);

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

    private NvaProject getTransformedProjectFromCristin(String id, String language) throws ApiGatewayException {
        return cristinApiClient.queryOneCristinProjectUsingIdIntoNvaProject(id, language);
    }

    private NvaProject authenticatedGetTransformedProjectFromCristin(String id, String language)
            throws ApiGatewayException {
        return new FetchCristinProjectApiClient(CristinAuthenticator.getHttpClient())
                .queryOneCristinProjectUsingIdIntoNvaProject(id, language);
    }

}
