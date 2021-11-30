package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, LANGUAGE, PAGE, NUMBER_OF_RESULTS);

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

        validateQueryParamKeys(requestInfo);

        String language = getValidLanguage(requestInfo);
        String query = getValidQuery(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        return getTransformedCristinProjectsUsingWrapperObject(language, query, page, numberOfResults);
    }

    @Override
    protected void validateQueryParamKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH);
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

        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(LANGUAGE, language);
        requestQueryParams.put(QUERY, query);
        requestQueryParams.put(PAGE, page);
        requestQueryParams.put(NUMBER_OF_RESULTS, numberOfResults);

        return cristinApiClient.queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(requestQueryParams);
    }

}
