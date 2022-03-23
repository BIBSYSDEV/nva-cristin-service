package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Set<String> VALID_QUERY_PARAMETERS = Set.of(QUERY, LANGUAGE, PAGE, NUMBER_OF_RESULTS);

    private final transient CristinApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchCristinProjects() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchCristinProjects(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    protected FetchCristinProjects(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParameterKeys(requestInfo);

        String language = getValidLanguage(requestInfo);
        String query = getValidQuery(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        return getTransformedCristinProjectsUsingWrapperObject(language, query, page, numberOfResults);
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

    private SearchResponse<NvaProject> getTransformedCristinProjectsUsingWrapperObject(String language,
                                                                                       String query,
                                                                                       String page,
                                                                                       String numberOfResults)
            throws ApiGatewayException {

        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
        requestQueryParameters.put(LANGUAGE, language);
        requestQueryParameters.put(QUERY, query);
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);

        return cristinApiClient.queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(requestQueryParameters);
    }

}
