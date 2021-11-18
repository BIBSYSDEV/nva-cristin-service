package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.common.handler.CristinQueryHandler.WORD_WHITESPACE_DASH_COMMA_PERIOD;
import static no.unit.nva.cristin.common.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.common.model.Constants.LANGUAGE;
import static no.unit.nva.cristin.common.model.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.PAGE;
import static no.unit.nva.cristin.common.model.Constants.QUERY;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_PAGE_VALUE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.common.util.UriUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends CristinHandler<Void, SearchResponse> {

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
    protected SearchResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateThatSuppliedQueryParamsIsSupported(requestInfo);

        String language = getValidLanguage(requestInfo);
        String query = getValidQuery(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        return getTransformedCristinProjectsUsingWrapperObject(language, query, page, numberOfResults);
    }

    private void validateThatSuppliedQueryParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
        return HttpURLConnection.HTTP_OK;
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

    private SearchResponse getTransformedCristinProjectsUsingWrapperObject(String language, String query, String page,
                                                                           String numberOfResults)
        throws ApiGatewayException {

        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(QUERY, query);
        requestQueryParams.put(LANGUAGE, language);
        requestQueryParams.put(PAGE, page);
        requestQueryParams.put(NUMBER_OF_RESULTS, numberOfResults);

        return cristinApiClient.queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(requestQueryParams);
    }

    private boolean isValidQuery(String str) {
        Pattern pattern = Pattern.compile(WORD_WHITESPACE_DASH_COMMA_PERIOD);
        return pattern.matcher(str).matches();
    }

}
