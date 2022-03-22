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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParametersMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;

@SuppressWarnings("unused")
public class QueryCristinPersonHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {


    private static final Set<String> VALID_QUERY_PARAMETERS = Set.of(NAME, ORGANIZATION,  PAGE, NUMBER_OF_RESULTS);
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

        validateQueryParameterKeys(requestInfo);

        String name = getValidName(requestInfo);
        Optional<String> organization = getValidOrganization(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        Map<String, String> requestQueryParameters = buildParametersMap(name, page, numberOfResults);
        organization.ifPresent(s -> requestQueryParameters.put(ORGANIZATION, s));

        return apiClient.generateQueryResponse(requestQueryParameters);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Person> output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParametersMessage(VALID_QUERY_PARAMETERS));
        }
    }


    private Map<String, String> buildParametersMap(String name, String page, String numberOfResults) {
        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
        requestQueryParameters.put(NAME, name);
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
        return requestQueryParameters;
    }
}
