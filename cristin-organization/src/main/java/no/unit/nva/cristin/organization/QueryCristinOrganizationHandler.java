package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;
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

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_LANGUAGE_INVALID;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static nva.commons.core.attempt.Try.attempt;

@SuppressWarnings("unused")
public class QueryCristinOrganizationHandler extends CristinQueryHandler<Void, SearchResponse<Organization>> {

    protected static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final char CHARACTER_DASH = '-';
    private static final char CHARACTER_COMMA = ',';
    private static final char CHARACTER_PERIOD = '.';
    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS);
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb", "nn");
    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public QueryCristinOrganizationHandler() {
        this(new CristinApiClient(), new Environment());
    }

    public QueryCristinOrganizationHandler(CristinApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = apiClient;
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }

    @Override
    protected SearchResponse<Organization> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateQueryParamKeys(requestInfo);

        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(QUERY, getValidQuery(requestInfo));
        requestQueryParams.put(PAGE, getValidPage(requestInfo));
        requestQueryParams.put(NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));

        try {
            return queryOrganizationsFromCristin(requestQueryParams);
        } catch (InterruptedException e) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Organization> output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateThatSuppliedQueryParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
    }

    private SearchResponse<Organization> queryOrganizationsFromCristin(Map<String, String> requestQueryParams)
            throws ApiGatewayException, InterruptedException {

        return Optional.of(cristinApiClient.queryInstitutions(requestQueryParams))
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }
}
