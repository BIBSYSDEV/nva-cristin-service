package no.unit.nva.cristin.common.handler;

import com.google.common.net.MediaType;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_VALUE;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static nva.commons.core.attempt.Try.attempt;

public abstract class CristinHandler<I, O> extends ApiGatewayHandler<I, O> {

    public static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb", "nn");

    public CristinHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    protected static String getValidLanguage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParameter(requestInfo, LANGUAGE)
            .orElse(DEFAULT_LANGUAGE_CODE))
            .filter(VALID_LANGUAGE_CODES::contains)
            .orElseThrow(() -> new BadRequestException(String.format(ERROR_MESSAGE_INVALID_VALUE, LANGUAGE)));
    }

    protected static Optional<String> getQueryParameter(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }

}
