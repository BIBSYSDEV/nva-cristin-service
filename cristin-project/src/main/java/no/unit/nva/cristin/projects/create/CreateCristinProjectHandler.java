package no.unit.nva.cristin.projects.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.unit.nva.Validator;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

import java.net.HttpURLConnection;
import java.util.List;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.utils.AccessUtils.verifyRequesterCanEditProjects;

public class CreateCristinProjectHandler extends ApiGatewayHandler<NvaProject, NvaProject> {

    private final transient CreateCristinProjectApiClient apiClient;

    /**
     * Create CreateCristinProjectHandler with default authenticated HttpClient.
     */
    @SuppressWarnings("unused")
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
        Validator<NvaProject> validator = new CreateCristinProjectValidator();
        validator.validate(input);

        return apiClient.createProjectInCristin(input);
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
