package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

@SuppressWarnings({"unused","PMD.SingularField"})
public class ListCristinOrganizationPersonsHandler extends ApiGatewayHandler<Void, SearchResponse<Person>> {

    private static final Logger logger = LoggerFactory.getLogger(FetchCristinPersonHandler.class);
    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    public ListCristinOrganizationPersonsHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public ListCristinOrganizationPersonsHandler(Environment environment) {
        this(new CristinPersonApiClient(), environment);
    }

    public ListCristinOrganizationPersonsHandler(CristinPersonApiClient apiClient, Environment environment) {
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
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        logger.debug("Will be processing input soon....");
        return null;
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Person> output) {
        return HttpURLConnection.HTTP_OK;
    }
}
