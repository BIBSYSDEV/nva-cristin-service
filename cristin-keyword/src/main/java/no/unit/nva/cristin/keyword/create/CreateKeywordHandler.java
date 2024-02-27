package no.unit.nva.cristin.keyword.create;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.access.HandlerAccessCheck;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.validation.Validator;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class CreateKeywordHandler extends ApiGatewayHandler<TypedLabel, TypedLabel> {

    private static final Logger logger = LoggerFactory.getLogger(CreateKeywordHandler.class);

    private final transient CreateKeywordApiClient apiClient;

    public CreateKeywordHandler() {
        this(new Environment(), new CristinCreateKeywordApiClient(CristinAuthenticator.getHttpClient()));
    }

    public CreateKeywordHandler(Environment environment,
                                CreateKeywordApiClient apiClient) {
        super(TypedLabel.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected TypedLabel processInput(TypedLabel input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        authorize(requestInfo);
        logUserInfo(requestInfo);
        validateInput(input);

        return apiClient.create(input);
    }

    @Override
    protected Integer getSuccessStatusCode(TypedLabel input, TypedLabel output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    private void authorize(RequestInfo requestInfo) throws ApiGatewayException {
        HandlerAccessCheck accessCheck = initAccessCheck();
        accessCheck.verifyAccess(requestInfo);
    }

    private HandlerAccessCheck initAccessCheck() {
        return new CreateKeywordHandlerAccessCheck();
    }

    private static void logUserInfo(RequestInfo requestInfo) {
        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
    }

    private void validateInput(TypedLabel input) throws ApiGatewayException {
        Validator<TypedLabel> validator = initValidator();
        validator.validate(input);
    }

    private Validator<TypedLabel> initValidator() {
        return new CristinCreateKeywordValidator();
    }
}
