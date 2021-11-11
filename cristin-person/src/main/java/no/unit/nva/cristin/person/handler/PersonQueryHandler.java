package no.unit.nva.cristin.person.handler;

import static no.unit.nva.cristin.common.model.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.PAGE;
import static no.unit.nva.cristin.common.model.Constants.QUERY;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.person.CristinPersonApiClient;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated // TODO: Will write tests later
public class PersonQueryHandler extends CristinQueryHandler<Void, SearchResponse> {

    public PersonQueryHandler() {
        super(Void.class, new Environment());
    }

    @Override
    protected SearchResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParamKeys(requestInfo);

        String query = getValidQuery(requestInfo); // TODO: Discuss validation of name query
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        Map<String, String> requestQueryParams = buildParamMap(query, page, numberOfResults);

        return new CristinPersonApiClient().generateQueryResponse(requestQueryParams);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
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
