package no.unit.nva.cristin.person.handler;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.person.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class PersonFetchHandler extends ApiGatewayHandler<Void, Person> {

    public static final String ID = "id";
    public static final String ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID =
        "Invalid path parameter for id, needs to be a number";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP =
        "This endpoint does not support query params";

    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    public PersonFetchHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public PersonFetchHandler(Environment environment) {
        this(new CristinPersonApiClient(), environment);
    }

    public PersonFetchHandler(CristinPersonApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Person processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateQueryParameters(requestInfo);

        String id = getValidId(requestInfo);

        return apiClient.generateGetResponse(id);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Person output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Integer.parseInt(requestInfo.getPathParameter(ID)))
            .orElseThrow(failure -> new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID));

        return requestInfo.getPathParameter(ID);
    }
}
