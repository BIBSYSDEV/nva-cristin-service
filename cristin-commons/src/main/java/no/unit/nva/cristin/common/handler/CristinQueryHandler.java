package no.unit.nva.cristin.common.handler;

import no.unit.nva.cristin.common.Utils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

import java.util.Set;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_VALUE;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;

public abstract class CristinQueryHandler<I, O> extends CristinHandler<I, O> {

    private static final Set<String> VALID_QUERY_PARAMETERS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS);

    public CristinQueryHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    protected String getValidPage(RequestInfo requestInfo) throws BadRequestException {
        var page = requestInfo.getQueryParameterOpt(PAGE).orElse(FIRST_PAGE);
        if (Utils.isPositiveInteger(page)) {
            return page;
        }
        throw new BadRequestException(String.format(ERROR_MESSAGE_INVALID_VALUE, PAGE));
    }

    protected String getValidNumberOfResults(RequestInfo requestInfo) throws BadRequestException {
        var results = requestInfo.getQueryParameterOpt(NUMBER_OF_RESULTS).orElse(DEFAULT_NUMBER_OF_RESULTS);
        if (Utils.isPositiveInteger(results)) {
            return results;
        }
        throw new BadRequestException(String.format(ERROR_MESSAGE_INVALID_VALUE, NUMBER_OF_RESULTS));
    }

}
