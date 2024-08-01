package no.unit.nva.cristin.keyword.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public class FetchKeywordHandler extends ApiGatewayHandler<Void, Keyword> {

    private final FetchApiClient<String, Keyword> apiClient;

    @SuppressWarnings("unused")
    public FetchKeywordHandler() {
        this(new Environment());
    }

    public FetchKeywordHandler(Environment environment) {
        this(new FetchCristinKeywordApiClient(), environment);
    }

    public FetchKeywordHandler(FetchApiClient<String, Keyword> apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Keyword processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var identifier = extractIdentifier(requestInfo);

        return apiClient.executeFetch(identifier);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Keyword output) {
        return HTTP_OK;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateIdentifier(requestInfo);
    }

    private void validateIdentifier(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Integer.parseInt(requestInfo.getPathParameter(ID)))
            .orElseThrow(fail -> notANumberException());
    }

    private static BadRequestException notANumberException() {
        return new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER);
    }

    private static String extractIdentifier(RequestInfo requestInfo) {
        return requestInfo.getPathParameter(ID);
    }

}
