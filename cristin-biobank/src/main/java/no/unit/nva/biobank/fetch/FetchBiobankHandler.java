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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchBiobankHandler extends CristinHandler<Void, Biobank> {

    private static final Logger logger = LoggerFactory.getLogger(FetchBiobankHandler.class);

    private final transient CristinBiobankApiClient cristinClient;

    @JacocoGenerated
    public FetchBiobankHandler() {
        this(CristinBiobankApiClient.defaultClient(), new Environment());
    }

    public FetchBiobankHandler(CristinBiobankApiClient cristinClient, Environment environment) {
        super(Void.class, environment);
        this.cristinClient = cristinClient;
    }

    @Override
    protected Biobank processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var query = (QueryBiobank)
                        QueryBiobank.builder()
                            .fromRequestInfo(requestInfo).build();
        logger.info("FETCH biobank -> " + query.toURI().toString());
        return
            cristinClient.fetchBiobank(query).toBiobank();
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Biobank output) {
        return HttpURLConnection.HTTP_OK;
    }



}
