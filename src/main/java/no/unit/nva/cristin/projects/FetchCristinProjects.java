package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.PAGE;
import static no.unit.nva.cristin.projects.Constants.QUERY;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_PAGE_VALUE_INVALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends CristinHandler<Void, ProjectsWrapper> {

    private static final char CHARACTER_DASH = '-';
    private static final char CHARACTER_COMMA = ',';
    private static final char CHARACTER_PERIOD = '.';

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
    protected ProjectsWrapper processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        String language = getValidLanguage(requestInfo);
        String query = getValidQuery(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);

        addContentTypeJsonLdToResponseIfRequested(requestInfo);

        return getTransformedCristinProjectsUsingWrapperObject(language, query, page, numberOfResults);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, ProjectsWrapper output) {
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

    private ProjectsWrapper getTransformedCristinProjectsUsingWrapperObject(String language, String query, String page,
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
