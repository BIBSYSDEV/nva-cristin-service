package no.unit.nva.cristin.person.handler;

import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class PersonQueryHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {

    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    public PersonQueryHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public PersonQueryHandler(Environment environment) {
        this(new CristinPersonApiClient(), environment);
    }

    public PersonQueryHandler(CristinPersonApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParamKeys(requestInfo);

        String query = getValidQuery(requestInfo); // TODO: Discuss validation of name query
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        Map<String, String> requestQueryParams = buildParamMap(query, page, numberOfResults);

        return apiClient.generateQueryResponse(requestQueryParams);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Person> output) {
        return HttpURLConnection.HTTP_OK;
    }

    private Map<String, String> buildParamMap(String query, String page, String numberOfResults) {
        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(QUERY, query);
        requestQueryParams.put(PAGE, page);
        requestQueryParams.put(NUMBER_OF_RESULTS, numberOfResults);
        return requestQueryParams;
    }
}
