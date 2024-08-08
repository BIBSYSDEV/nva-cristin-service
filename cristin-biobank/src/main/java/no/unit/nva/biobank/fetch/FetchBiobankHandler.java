package no.unit.nva.biobank.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.biobank.client.CristinBiobankApiClient;
import no.unit.nva.biobank.model.QueryBiobank;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.cristin.common.handler.CristinHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchBiobankHandler extends CristinHandler<Void, Biobank> {

    private final transient CristinBiobankApiClient cristinClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public FetchBiobankHandler() {
        this(CristinBiobankApiClient.defaultClient(), new Environment());

    }

    public FetchBiobankHandler(CristinBiobankApiClient cristinClient, Environment environment) {
        super(Void.class, environment);
        this.cristinClient = cristinClient;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) {
        // no-op
    }

    @Override
    protected Biobank processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var query = (QueryBiobank) QueryBiobank.builder()
                                       .fromRequestInfo(requestInfo)
                                       .build();

        return cristinClient.fetchBiobank(query);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Biobank output) {
        return HttpURLConnection.HTTP_OK;
    }



}
