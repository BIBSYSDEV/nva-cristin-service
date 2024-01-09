package no.unit.nva.cristin.projects.category;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.TypedLabel;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class ListCategoriesHandler extends ApiGatewayHandler<Void, SearchResponse<TypedLabel>> {

    private final transient CristinQueryApiClient<Void, TypedLabel> apiClient;

    @SuppressWarnings("unused")
    public ListCategoriesHandler() {
        this(new CategoryApiClient(), new Environment());
    }

    public ListCategoriesHandler(CristinQueryApiClient<Void, TypedLabel> apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected SearchResponse<TypedLabel> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        return apiClient.executeQuery(null);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<TypedLabel> output) {
        return HttpURLConnection.HTTP_OK;
    }

}
