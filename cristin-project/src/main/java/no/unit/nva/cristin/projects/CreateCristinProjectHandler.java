package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;

import java.net.HttpURLConnection;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.core.attempt.Try.attempt;

public class CreateCristinProjectHandler extends ApiGatewayHandler<NvaProject, NvaProject> {

    public static final String NEEDED_ROLE = "Creator";

    public CreateCristinProjectHandler(Environment environment) {
        super(NvaProject.class, environment);
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
    protected NvaProject processInput(NvaProject input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        validateAccess(requestInfo);
        validateInput(input);

        return dummyPersistProject(input);
    }

    private void validateInput(NvaProject project) throws BadRequestException {
        attempt(() -> project.toCristinProject().hasValidContent()).orElseThrow(failure -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    private void validateAccess(RequestInfo requestInfo) throws ForbiddenException {
        boolean hasAccess = attempt(() -> requestInfo.getAssignedRoles().get().contains(NEEDED_ROLE)).orElseThrow(failure -> new ForbiddenException());
        if (!hasAccess) {
            throw new ForbiddenException();
        }
    }

    private NvaProject dummyPersistProject(NvaProject input) {
        input.setId(randomUri());
        return input;
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
