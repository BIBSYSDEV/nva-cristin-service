package no.unit.nva.cristin.person.country;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.cristin.person.model.nva.Countries;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

@SuppressWarnings("unused")
public class ListCountriesHandler extends ApiGatewayHandler<Void, Countries> {

    private final ListCountriesApiClient apiClient;

    public ListCountriesHandler() {
        this(new Environment(), new ListCountriesApiClient());
    }

    public ListCountriesHandler(Environment environment, ListCountriesApiClient apiClient) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Countries processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        return apiClient.executeRequest();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Countries output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

}
