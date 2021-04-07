package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import java.util.Optional;
import java.util.Set;
import nva.commons.apigateway.RequestInfo;

public class RequestUtils {

    protected static final String LANGUAGE_QUERY_PARAMETER = "language";
    protected static final String LANGUAGE_INVALID_ERROR_MESSAGE = "Parameter 'language' has invalid value";
    protected static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb");

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
