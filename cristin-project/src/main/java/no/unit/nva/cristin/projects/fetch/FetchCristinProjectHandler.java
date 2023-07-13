package no.unit.nva.cristin.projects.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.common.handler.CristinHandler;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.exception.UnauthorizedException;
import no.unit.nva.utils.HandlerAccessCheck;
import no.unit.nva.utils.ResourceAccessCheck;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import static no.unit.nva.cristin.projects.update.UpdateProjectResourceAccessCheck.USER_IDENTIFIER;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;

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
    protected NvaProject processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateQueryParameters(requestInfo);
        var id = getValidIdentifier(requestInfo);

        try {
            return getTransformedProjectFromCristin(id);
        } catch (UnauthorizedException unauthorizedException) {
            if (doesNotHaveHandlerAccess(requestInfo)) {
                throw new ForbiddenException();
            }

            var requestedProject =  authenticatedFetchProject(id);

            if (doesNotHaveResourceAccess(requestInfo, requestedProject)) {
                throw new ForbiddenException();
            }

            return requestedProject;
        }
    }

    private boolean doesNotHaveResourceAccess(RequestInfo requestInfo, NvaProject requestedProject)
        throws ApiGatewayException {

        ResourceAccessCheck<NvaProject> resourceAccessCheck = new FetchCristinProjectResourceAccessCheck();
        resourceAccessCheck.verifyAccess(requestedProject,
                                         Map.of(USER_IDENTIFIER, extractCristinIdentifier(requestInfo)));

        return !resourceAccessCheck.isVerified();
    }

    private boolean doesNotHaveHandlerAccess(RequestInfo requestInfo) throws ApiGatewayException {
        HandlerAccessCheck handlerAccessCheck = new FetchCristinProjectHandlerAccessCheck();
        handlerAccessCheck.verifyAccess(requestInfo);

        return !handlerAccessCheck.isVerified();
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

    private NvaProject authenticatedFetchProject(String id) throws ApiGatewayException {
        return authorizedApiClient().queryOneCristinProjectUsingIdIntoNvaProject(id);
    }

    protected FetchCristinProjectApiClient authorizedApiClient() {
        return new FetchCristinProjectApiClient(CristinAuthenticator.getHttpClient());
    }

}
