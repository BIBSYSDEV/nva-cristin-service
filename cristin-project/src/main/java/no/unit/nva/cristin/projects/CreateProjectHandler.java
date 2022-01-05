package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.HttpUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.net.Authenticator;
import java.net.HttpURLConnection;

import static java.util.Objects.isNull;

public class CreateProjectHandler extends ApiGatewayHandler<NvaProject, NvaProject> {

    private static final String MISSING_REQUIRED_FIELDS_MESSAGE =
            "Create project request is missing one of required fields; "
                    + "'title', 'coordinating_institution', 'start_date' 'participants' ";
    private final transient AuthenticatedCristinApiClient cristinApiClient;

    public CreateProjectHandler() {
        this(new HttpUtils().getBasicAuthenticator());
    }

    public CreateProjectHandler(Authenticator basicAuthenticator) {
        super(NvaProject.class);
        this.cristinApiClient = new AuthenticatedCristinApiClient(basicAuthenticator);
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

        validate(input);

        return createCristinProject(input);
    }

    private NvaProject createCristinProject(NvaProject input) {
        return cristinApiClient.createNvaProject(input);
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
        return HttpURLConnection.HTTP_OK;
    }

    private void validate(NvaProject input) throws BadRequestException {
        if (isNull(input)
                || isNull(input.getTitle())
                || isNull(input.getCoordinatingInstitution())
                || isNull(input.getStartDate())
                || isNull(input.getContributors())
        ) {
            throw new BadRequestException(MISSING_REQUIRED_FIELDS_MESSAGE);
        }
    }

}
