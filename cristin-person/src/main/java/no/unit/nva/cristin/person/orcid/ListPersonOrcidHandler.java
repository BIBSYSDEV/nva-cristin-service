package no.unit.nva.cristin.person.orcid;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.util.List;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.cristin.person.orcid.model.PersonsOrcid;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class ListPersonOrcidHandler extends ApiGatewayHandler<Void, PersonsOrcid> {

    private final FetchApiClient<Void, PersonsOrcid> apiClient;

    @SuppressWarnings("unused")
    public ListPersonOrcidHandler() {
        this(new Environment(), new ListPersonOrcidApiClient());
    }

    public ListPersonOrcidHandler(Environment environment, FetchApiClient<Void, PersonsOrcid> apiClient) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected PersonsOrcid processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        return apiClient.executeFetch(null);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PersonsOrcid output) {
        return HTTP_OK;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

}
