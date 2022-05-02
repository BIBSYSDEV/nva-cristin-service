package no.unit.nva.cristin.person.affiliations.handler;

import static no.unit.nva.cristin.common.ErrorMessages.ONLY_SUPPORT_BOOLEAN_VALUES;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.person.affiliations.client.CristinPositionCodesClient;
import no.unit.nva.cristin.person.affiliations.model.PositionCodes;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

@SuppressWarnings("unused")
public class PositionCodesHandler extends ApiGatewayHandler<Void, PositionCodes> {

    public static final String ACTIVE_STATUS_QUERY_PARAM = "active";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    private final transient CristinPositionCodesClient apiClient;

    @JacocoGenerated
    public PositionCodesHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public PositionCodesHandler(Environment environment) {
        this(new CristinPositionCodesClient(), environment);
    }

    public PositionCodesHandler(CristinPositionCodesClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected PositionCodes processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        return apiClient.generateQueryResponse(extractPositionCodeStatusQueryParam(requestInfo));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PositionCodes output) {
        return HttpURLConnection.HTTP_OK;
    }

    private Boolean extractPositionCodeStatusQueryParam(RequestInfo requestInfo) throws BadRequestException {
        var activeStatusQueryParam = requestInfo.getQueryParameterOpt(ACTIVE_STATUS_QUERY_PARAM)
            .map(String::valueOf).orElse(null);
        return StringUtils.isEmpty(activeStatusQueryParam) ? null
                   : validateAndGetPositionCodeStatusBooleanQueryParam(activeStatusQueryParam);
    }

    private Boolean validateAndGetPositionCodeStatusBooleanQueryParam(String activeStatusQueryParam)
        throws BadRequestException {
        if (TRUE.equalsIgnoreCase(activeStatusQueryParam) || FALSE.equalsIgnoreCase(activeStatusQueryParam)) {
            return Boolean.valueOf(activeStatusQueryParam);
        } else {
            throw new BadRequestException(invalidQueryParametersMessage(
                ACTIVE_STATUS_QUERY_PARAM, ONLY_SUPPORT_BOOLEAN_VALUES));
        }
    }
}
