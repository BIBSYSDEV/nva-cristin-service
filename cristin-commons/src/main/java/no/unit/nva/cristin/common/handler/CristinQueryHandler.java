package no.unit.nva.cristin.common.handler;

import static no.unit.nva.cristin.common.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.common.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.common.model.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.PAGE;
import static no.unit.nva.cristin.common.model.Constants.QUERY;
import static nva.commons.core.attempt.Try.attempt;
import com.google.common.net.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import no.unit.nva.cristin.common.util.UriUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated // TODO: When FetchCristinProjects extends this we will get testing for free
public abstract class CristinQueryHandler<I, O> extends ApiGatewayHandler<I, O> {

    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH =
        "Invalid query param supplied. Valid ones are 'query', 'page' and 'results'";
    public static final String ERROR_MESSAGE_PAGE_VALUE_INVALID = "Parameter 'page' has invalid value";
    public static final String ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID = "Parameter 'results' has invalid value";
    public static final String ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS =
        "Parameter 'query' is missing or invalid. "
            + "May only contain alphanumeric characters, dash, comma, period and whitespace";

    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS);

    public static final String WORD_WHITESPACE_DASH_COMMA_PERIOD = "[\\w\\s\\-,.]+";

    public CristinQueryHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    protected void validateQueryParamKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH);
        }
    }

    protected String getValidPage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, PAGE)
                .orElse(FIRST_PAGE))
            .filter(this::isPositiveInteger)
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_PAGE_VALUE_INVALID));
    }

    protected String getValidNumberOfResults(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, NUMBER_OF_RESULTS)
                .orElse(DEFAULT_NUMBER_OF_RESULTS))
            .filter(this::isPositiveInteger)
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID));
    }

    protected String getValidQuery(RequestInfo requestInfo) throws BadRequestException {
        return getQueryParam(requestInfo, QUERY)
            .filter(this::isValidQuery)
            .map(UriUtils::escapeWhiteSpace)
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    private boolean isPositiveInteger(String str) {
        try {
            int value = Integer.parseInt(str);
            return value > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidQuery(String str) {
        Pattern pattern = Pattern.compile(WORD_WHITESPACE_DASH_COMMA_PERIOD);
        return pattern.matcher(str).matches();
    }

}
