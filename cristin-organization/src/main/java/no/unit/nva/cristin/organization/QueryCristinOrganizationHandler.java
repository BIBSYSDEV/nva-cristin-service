package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_LANGUAGE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_PAGE_VALUE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import static nva.commons.core.attempt.Try.attempt;

@SuppressWarnings("unused")
public class QueryCristinOrganizationHandler extends ApiGatewayHandler<Void, SearchResponse<Organization>> {

    protected static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final char CHARACTER_DASH = '-';
    private static final char CHARACTER_COMMA = ',';
    private static final char CHARACTER_PERIOD = '.';
    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, LANGUAGE, PAGE, NUMBER_OF_RESULTS);
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb", "nn");
    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public QueryCristinOrganizationHandler() {
        this(new CristinApiClient());
    }

    public QueryCristinOrganizationHandler(CristinApiClient cristinApiClient) {
        super(Void.class);
        this.cristinApiClient = cristinApiClient;
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }

    protected static String getValidLanguage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, LANGUAGE)
                        .orElse(DEFAULT_LANGUAGE_CODE))
                .filter(VALID_LANGUAGE_CODES::contains)
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    @Override
    protected SearchResponse<Organization> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateThatSuppliedQueryParamsIsSupported(requestInfo);

        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(LANGUAGE, getValidLanguage(requestInfo));
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

    private String getValidQuery(RequestInfo requestInfo) throws BadRequestException {
        return getQueryParam(requestInfo, QUERY)
                .filter(this::isValidQuery)
                .map(UriUtils::escapeWhiteSpace)
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    private String getValidPage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, PAGE)
                        .orElse(FIRST_PAGE))
                .filter(Utils::isPositiveInteger)
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_PAGE_VALUE_INVALID));
    }

    private String getValidNumberOfResults(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, NUMBER_OF_RESULTS)
                        .orElse(DEFAULT_NUMBER_OF_RESULTS))
                .filter(Utils::isPositiveInteger)
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID));
    }

    private boolean isValidQuery(String str) {
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (!isValidCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidCharacter(char c) {
        return Character.isWhitespace(c)
                || Character.isLetterOrDigit(c)
                || c == CHARACTER_DASH
                || c == CHARACTER_COMMA
                || c == CHARACTER_PERIOD;
    }

}
