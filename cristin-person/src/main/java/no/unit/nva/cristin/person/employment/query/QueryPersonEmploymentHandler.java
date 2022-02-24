package no.unit.nva.cristin.person.employment.query;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PERSON_ID;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class QueryPersonEmploymentHandler extends ApiGatewayHandler<Void, CristinPersonEmployment> {

    private final transient QueryPersonEmploymentClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public QueryPersonEmploymentHandler() {
        this(new QueryPersonEmploymentClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public QueryPersonEmploymentHandler(QueryPersonEmploymentClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected CristinPersonEmployment processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        String identifier = getValidPersonId(requestInfo);

        return apiClient.queryUpstreamUsingIdentifier(identifier);
    }

    protected String getValidPersonId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(PERSON_ID)).orElse(fail -> EMPTY_STRING);
        if (isValidIdentifier(identifier)) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PERSON_ID);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, CristinPersonEmployment output) {
        return HttpURLConnection.HTTP_OK;
    }

    private boolean isValidIdentifier(String identifier) {
        return Utils.isPositiveInteger(identifier);
    }
}
