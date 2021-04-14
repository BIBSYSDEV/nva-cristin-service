package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import java.util.Optional;
import java.util.Set;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;

public abstract class CristinHandler<I, O> extends ApiGatewayHandler<I, O> {

    protected static final String LANGUAGE_QUERY_PARAMETER = "language";
    protected static final String LANGUAGE_INVALID_ERROR_MESSAGE = "Parameter 'language' has invalid value";
    protected static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb");

    public CristinHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    protected static String getValidLanguage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, LANGUAGE_QUERY_PARAMETER)
            .orElse(DEFAULT_LANGUAGE_CODE))
            .filter(VALID_LANGUAGE_CODES::contains)
            .orElseThrow(() -> new BadRequestException(LANGUAGE_INVALID_ERROR_MESSAGE));
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }

}
