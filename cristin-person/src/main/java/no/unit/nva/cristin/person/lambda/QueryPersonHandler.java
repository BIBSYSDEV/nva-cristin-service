package no.unit.nva.cristin.person.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.common.model.SearchResponse;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class QueryPersonHandler extends ApiGatewayHandler<Void, SearchResponse> {

    public QueryPersonHandler() {
        super(Void.class, new Environment());
    }

    @Override
    protected SearchResponse processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {
        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
        return HttpURLConnection.HTTP_OK;
    }
}
