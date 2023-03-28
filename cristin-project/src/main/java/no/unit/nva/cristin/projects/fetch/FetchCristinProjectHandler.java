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

import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import static no.unit.nva.utils.AccessUtils.verifyRequesterCanEditProjects;

public class FetchCristinProjectHandler extends CristinHandler<Void, NvaProject> {

    public static final String ERROR_MESSAGE_CLIENT_SENT_UNSUPPORTED_QUERY_PARAM =
        "This endpoint does not support query parameters";
    private final transient FetchCristinProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchCristinProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchCristinProjectHandler(Environment environment) {
        this(new FetchCristinProjectApiClient(), environment);
    }

    public FetchCristinProjectHandler(FetchCristinProjectApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected NvaProject processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateQueryParameters(requestInfo);
        var id = getValidIdentifier(requestInfo);

        try {
            return getTransformedProjectFromCristin(id);
        } catch (UnauthorizedException unauthorizedException) {
            verifyRequesterCanEditProjects(requestInfo);
            return authenticatedGetTransformedProjectFromCristin(id);
        }
    }

    private void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_CLIENT_SENT_UNSUPPORTED_QUERY_PARAM);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, NvaProject output) {
        return HttpURLConnection.HTTP_OK;
    }

    private NvaProject getTransformedProjectFromCristin(String id) throws ApiGatewayException {
        return cristinApiClient.queryOneCristinProjectUsingIdIntoNvaProject(id);
    }

    private NvaProject authenticatedGetTransformedProjectFromCristin(String id)
            throws ApiGatewayException {
        return new FetchCristinProjectApiClient(CristinAuthenticator.getHttpClient())
                .queryOneCristinProjectUsingIdIntoNvaProject(id);
    }

}
