package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.projects.CristinQuery.CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID;

@SuppressWarnings({"Unused", "UnusedPrivateField"})
public class ListCristinOrganizationProjectsHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    private final transient CristinApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public ListCristinOrganizationProjectsHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public ListCristinOrganizationProjectsHandler(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    protected ListCristinOrganizationProjectsHandler(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
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
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        Map<String, String> requestQueryParameters = Map.of(
                CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID, requestInfo.getPathParameter(ID),
                LANGUAGE, getValidLanguage(requestInfo),
                PAGE, getValidPage(requestInfo),
                NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));

        return cristinApiClient.listOrganizationProjects(requestQueryParameters);
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<NvaProject> output) {
        return HttpURLConnection.HTTP_OK;
    }
}
