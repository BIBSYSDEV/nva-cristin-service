package no.unit.nva.cristin.person.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.utils.AccessUtils.DOING_AUTHORIZED_REQUEST;
import static no.unit.nva.utils.AccessUtils.clientIsCustomerAdministrator;
import static no.unit.nva.utils.AccessUtils.requesterIsUserAdministrator;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

public class FetchCristinPersonHandler extends ApiGatewayHandler<Void, Person> {

    private static final Logger logger = LoggerFactory.getLogger(FetchCristinPersonHandler.class);

    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public FetchCristinPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchCristinPersonHandler(Environment environment) {
        this(new CristinPersonApiClient(), environment);
    }

    public FetchCristinPersonHandler(CristinPersonApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Person processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var identifier = getValidId(requestInfo);

        if (clientIsAuthorized(requestInfo)) {
            logger.info(DOING_AUTHORIZED_REQUEST);
            logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

            return apiClient.authorizedGenerateGetResponse(identifier);
        } else {
            return apiClient.generateGetResponse(identifier);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Person output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateQueryParameters(requestInfo);
    }

    private void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        var identifier = requestInfo.getPathParameter(ID);
        if (isValidIdentifier(identifier)) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID);
    }

    private boolean isValidIdentifier(String identifier) {
        return Utils.isPositiveInteger(identifier) || Utils.isOrcid(identifier);
    }

    private boolean clientIsAuthorized(RequestInfo requestInfo) {
        return requesterIsUserAdministrator(requestInfo) || clientIsCustomerAdministrator(requestInfo);
    }

}
