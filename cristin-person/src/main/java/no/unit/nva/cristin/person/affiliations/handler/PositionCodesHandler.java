package no.unit.nva.cristin.person.affiliations.handler;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.person.affiliations.client.CristinPositionCodesClient;
import no.unit.nva.cristin.person.affiliations.model.PositionCodes;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
public class PositionCodesHandler extends ApiGatewayHandler<Void, PositionCodes> {

    public static final String ACTIVE_STATUS_QUERY_PARAM = "active";
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

        return apiClient.generateQueryResponse(extractPositionCodeStatusLookingFor(requestInfo));
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PositionCodes output) {
        return HttpURLConnection.HTTP_OK;
    }

    private Boolean extractPositionCodeStatusLookingFor(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(ACTIVE_STATUS_QUERY_PARAM)
            .map(Boolean::valueOf).orElse(null);
    }
}
