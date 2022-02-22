package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.StringUtils;

import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.utils.AccessUtils.verifyRequesterCanEditProjects;

public class CreateCristinProjectHandler extends ApiGatewayHandler<NvaProject, NvaProject> {

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
     * @param apiClient   HttpClient to use for access external services
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

        verifyRequesterCanEditProjects(requestInfo);
        validateInput(input);

        return apiClient.createProjectInCristin(input);
    }

    private void validateInput(NvaProject project) throws BadRequestException {
        if (isNull(project)
            || hasId(project)
            || noTitle(project)
            || invalidStartDate(project.getStartDate())
            || hasNoContributors(project.getContributors())
            || hasNoCoordinatingOrganization(project.getCoordinatingInstitution())
        ) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
    }

    private boolean hasNoCoordinatingOrganization(Organization coordinatingInstitution) {
        return isNull(coordinatingInstitution);
    }

    private boolean hasNoContributors(List<NvaContributor> contributors) {
        return isNull(contributors) || contributors.isEmpty();
    }

    private boolean invalidStartDate(Instant startDate) {
        return isNull(startDate);
    }

    private boolean hasId(NvaProject project) {
        return Objects.nonNull(project.getId());
    }

    private boolean noTitle(NvaProject project) {
        return StringUtils.isEmpty(project.getTitle());
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

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

}
