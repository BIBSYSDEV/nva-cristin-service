package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.StringUtils;

import java.net.HttpURLConnection;
import java.util.Objects;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static nva.commons.core.attempt.Try.attempt;

public class CreateCristinProjectHandler extends ApiGatewayHandler<NvaProject, NvaProject> {

    public static final String NEEDED_ROLE = "Creator";
    private final transient CreateCristinProjectApiClient apiClient;

    /**
     * Create CreateCristinProjectHandler with default authenticated HttpClient.
     */
    public CreateCristinProjectHandler() {
        this(new CreateCristinProjectApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    /**
     * Create CreateCristinProjectHandler with supplied HttpClient.
     *
     * @param apiClient HttpClient to use for access external services
     * @param environment configuration for service
     */
    public CreateCristinProjectHandler(CreateCristinProjectApiClient apiClient, Environment environment) {
        super(NvaProject.class, environment);
        this.apiClient = apiClient;
    }

    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param input       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected NvaProject processInput(NvaProject input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateAccess(requestInfo);
        validateInput(input);

        return apiClient.createProjectInCristin(input);
    }

    private void validateInput(NvaProject project) throws BadRequestException {
        if (hasId(project) || noTitle(project) || invalidStatus(project)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
    }

    private boolean hasId(NvaProject project) {
        return Objects.nonNull(project.getId());
    }

    private boolean invalidStatus(NvaProject project) {
        return !ProjectStatus.isValidStatus(project.getStatus().name());
    }

    private boolean noTitle(NvaProject project) {
        return StringUtils.isEmpty(project.getTitle());
    }

    private void validateAccess(RequestInfo requestInfo) throws ForbiddenException {
        boolean hasAccess = attempt(() -> requestInfo.getAssignedRoles()
                .get().contains(NEEDED_ROLE)).orElseThrow(failure -> new ForbiddenException());
        if (!hasAccess) {
            throw new ForbiddenException();
        }
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(NvaProject input, NvaProject output) {
        return HttpURLConnection.HTTP_CREATED;
    }

}
