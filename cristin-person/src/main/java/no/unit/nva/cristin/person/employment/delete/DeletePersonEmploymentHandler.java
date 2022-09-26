package no.unit.nva.cristin.person.employment.delete;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;

import java.net.HttpURLConnection;

import static no.unit.nva.cristin.common.Utils.generateInstNrHeader;
import static no.unit.nva.cristin.common.Utils.getValidEmploymentId;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;

public class DeletePersonEmploymentHandler extends ApiGatewayHandler<Void, Void> {

    private final transient DeletePersonEmploymentClient apiClient;

    @SuppressWarnings("unused")
    public DeletePersonEmploymentHandler() {
        this(new DeletePersonEmploymentClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public DeletePersonEmploymentHandler(DeletePersonEmploymentClient apiClient, Environment environment) {
        super(Void.class, environment);
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
    protected Void processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateHasAccessRights(requestInfo);
        String personId = getValidPersonId(requestInfo);
        String employmentId = getValidEmploymentId(requestInfo);
        return apiClient.deletePersonEmployment(personId, employmentId, generateInstNrHeader(requestInfo));
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, Void output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }
}
