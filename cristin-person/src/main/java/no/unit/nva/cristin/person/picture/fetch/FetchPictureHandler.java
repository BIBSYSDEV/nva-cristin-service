package no.unit.nva.cristin.person.picture.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.nva.Binary;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPictureHandler extends ApiGatewayHandler<Void, Binary> {
    private final transient FetchPictureApiClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchPictureHandler() {
        this(new FetchPictureApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public FetchPictureHandler(FetchPictureApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Binary processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var identifier = getValidPersonId(requestInfo);

        return apiClient.fetchPicture(identifier);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Binary output) {
        return HTTP_OK;
    }
}
