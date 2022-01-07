package no.unit.nva.cristin.common.handler;

import no.unit.nva.cristin.common.Utils;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

import java.util.List;
import java.util.Set;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_PAGE_VALUE_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.Constants.FULL;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;

public abstract class CristinQueryHandler<I, O> extends CristinHandler<I, O> {

    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS, DEPTH);
    private static final List<Character> VALID_SPECIAL_CHARS = List.of('-', ',', '.');

    public CristinQueryHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
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

    protected String getValidDepth(RequestInfo requestInfo) throws BadRequestException {
        isValidDepth(requestInfo);
        return requestInfo.getQueryParameters().containsKey(DEPTH)
                ? requestInfo.getQueryParameter(DEPTH)
                : TOP;
    }

    private boolean isValidQuery(String str) {
        for (Character c : str.toCharArray()) {
            if (isUnsupportedCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidDepth(RequestInfo requestInfo) throws BadRequestException {
        return !requestInfo.getQueryParameters().containsKey(DEPTH) || Set.of(TOP, FULL).contains(requestInfo.getQueryParameter(DEPTH));
    }


    private boolean isUnsupportedCharacter(Character c) {
        return !Character.isLetterOrDigit(c) && !Character.isWhitespace(c) && !VALID_SPECIAL_CHARS.contains(c);
    }
}
