package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_LANGUAGE_INVALID;
import static nva.commons.core.attempt.Try.attempt;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.HttpHeaders;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public abstract class CristinHandler<I, O> extends ApiGatewayHandler<I, O> {

    protected static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb", "nn");
    private static final Set<String> VALID_CONTENT_TYPES = Set.of("application/json", "application/ld+json");
    private final transient Map<String, String> additionalHeaders;

    public CristinHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
        this.additionalHeaders = new ConcurrentHashMap<>();
    }

    @Override
    protected Map<String, String> defaultHeaders() {
        Map<String, String> headers = super.defaultHeaders();
        headers.putAll(additionalHeaders);
        return headers;
    }

    protected static String getValidLanguage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, LANGUAGE)
            .orElse(DEFAULT_LANGUAGE_CODE))
            .filter(VALID_LANGUAGE_CODES::contains)
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }

    protected void addRequestedContentTypeToResponseIfSupported(RequestInfo requestInfo) {
        getRequestedContentType(requestInfo)
            .map(String::toLowerCase)
            .filter(VALID_CONTENT_TYPES::contains)
            .ifPresent(this::addContentTypeHeader);
    }

    private void addContentTypeHeader(String contentType) {
        this.additionalHeaders.put(HttpHeaders.CONTENT_TYPE, contentType);
    }

    private Optional<String> getRequestedContentType(RequestInfo requestInfo) {
        return Optional.ofNullable(requestInfo)
            .map(RequestInfo::getHeaders)
            .map(headers -> headers.get(HttpHeaders.ACCEPT));
    }
}
