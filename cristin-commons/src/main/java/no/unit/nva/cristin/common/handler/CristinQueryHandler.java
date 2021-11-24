package no.unit.nva.cristin.common.handler;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_PAGE_VALUE_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static nva.commons.core.attempt.Try.attempt;
import com.google.common.net.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated // TODO: When FetchCristinProjects extends this we will get testing for free
public abstract class CristinQueryHandler<I, O> extends ApiGatewayHandler<I, O> {

    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS);

    public static final String WORD_WHITESPACE_DASH_COMMA_PERIOD = "[\\w\\s\\-,.]+";
    public static final Pattern QUERY_PATTERN = Pattern.compile(WORD_WHITESPACE_DASH_COMMA_PERIOD);

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
        String page = getQueryParam(requestInfo, PAGE).orElse(FIRST_PAGE);
        if (Utils.isPositiveInteger(page)) {
            return page;
        }
        throw new BadRequestException(ERROR_MESSAGE_PAGE_VALUE_INVALID);
    }

    protected String getValidNumberOfResults(RequestInfo requestInfo) throws BadRequestException {
        String results = getQueryParam(requestInfo, NUMBER_OF_RESULTS).orElse(DEFAULT_NUMBER_OF_RESULTS);
        if (Utils.isPositiveInteger(results)) {
            return results;
        }
        throw new BadRequestException(ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID);
    }

    protected String getValidQuery(RequestInfo requestInfo) throws BadRequestException {
        return getQueryParam(requestInfo, QUERY)
            .filter(this::isValidQuery)
            .map(UriUtils::escapeWhiteSpace)
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    private boolean isValidQuery(String str) {
        return QUERY_PATTERN.matcher(str).matches();
    }

}
