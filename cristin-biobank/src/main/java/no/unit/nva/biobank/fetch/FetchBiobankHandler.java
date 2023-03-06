package no.unit.nva.biobank.fetch;

import static no.unit.nva.biobank.client.CristinBiobankApiClient.defaultClient;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import no.unit.nva.biobank.client.CristinBiobankApiClient;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.cristin.common.handler.CristinHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchBiobankHandler extends CristinHandler<Void, Biobank> {

    private static final String CODE_PATH_PARAMETER_NAME = "identifier";
    private final transient CristinBiobankApiClient cristinClient;

    @JacocoGenerated
    public FetchBiobankHandler() {
        this(defaultClient(), new Environment());
    }

    public FetchBiobankHandler(CristinBiobankApiClient cristinClient, Environment environment) {
        super(Void.class, environment);
        this.cristinClient = cristinClient;
    }

    @Override
    protected Biobank processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var urlEncodedIdentifier = requestInfo.getPathParameter(CODE_PATH_PARAMETER_NAME);
        var identifier = URLDecoder.decode(urlEncodedIdentifier, StandardCharsets.UTF_8);
        var cristinFundingSource = cristinClient.fetchBiobank(identifier);

        return cristinFundingSource.toBiobank();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Biobank output) {
        return HttpURLConnection.HTTP_OK;
    }
}
