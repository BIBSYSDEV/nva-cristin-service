package no.unit.nva.cristin.projects.category;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
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

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

}
