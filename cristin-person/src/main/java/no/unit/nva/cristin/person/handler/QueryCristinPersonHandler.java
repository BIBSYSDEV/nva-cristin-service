package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_SEARCH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;

@SuppressWarnings("unused")
public class QueryCristinPersonHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {


    private static final Set<String> VALID_QUERY_PARAMS = Set.of(NAME, PAGE, NUMBER_OF_RESULTS);
    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    public QueryCristinPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinPersonHandler(Environment environment) {
        this(new CristinPersonApiClient(), environment);
    }

    public QueryCristinPersonHandler(CristinPersonApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParamKeys(requestInfo);

        String name = getValidName(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        Map<String, String> requestQueryParams = buildParamMap(name, page, numberOfResults);

        return apiClient.generateQueryResponse(requestQueryParams);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Person> output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void validateQueryParamKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_SEARCH);
        }
    }


    private Map<String, String> buildParamMap(String name, String page, String numberOfResults) {
        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(NAME, name);
        requestQueryParams.put(PAGE, page);
        requestQueryParams.put(NUMBER_OF_RESULTS, numberOfResults);
        return requestQueryParams;
    }
}
