package no.unit.nva.cristin.projects.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.unit.nva.Validator;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.projects.common.CreateProjectAccessCheck;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.AccessCheck;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

import java.net.HttpURLConnection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

public class CreateCristinProjectHandler extends ApiGatewayHandler<NvaProject, NvaProject> {

    private static final Logger logger = LoggerFactory.getLogger(CreateCristinProjectHandler.class);

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

        AccessCheck accessCheck = new CreateProjectAccessCheck();
        accessCheck.verifyAccess(requestInfo);
        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
        Validator<NvaProject> validator = new CreateCristinProjectValidator();
        validator.validate(input);
        addCreatorDataToInput(input, requestInfo);

        return apiClient.createProjectInCristin(input);
    }

    private void addCreatorDataToInput(NvaProject input, RequestInfo requestInfo) {
        var creator = new ProjectCreatorAppender(requestInfo).getCreator();
        input.setCreator(creator);
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
