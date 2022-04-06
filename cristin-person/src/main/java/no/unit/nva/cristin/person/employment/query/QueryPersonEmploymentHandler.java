package no.unit.nva.cristin.person.employment.query;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.List;

import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;

public class QueryPersonEmploymentHandler extends ApiGatewayHandler<Void, SearchResponse<Employment>> {

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
    protected SearchResponse<Employment> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        String identifier = getValidPersonId(requestInfo);

        return apiClient.generateQueryResponse(identifier);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Employment> output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

}
