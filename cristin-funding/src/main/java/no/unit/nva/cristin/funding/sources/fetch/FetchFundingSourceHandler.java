package no.unit.nva.cristin.funding.sources.fetch;

import static no.unit.nva.cristin.funding.sources.client.CristinFundingSourcesApiClient.defaultClient;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_BASE_PATH;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_DOMAIN_NAME;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import no.unit.nva.cristin.common.handler.CristinHandler;
import no.unit.nva.cristin.funding.sources.client.CristinFundingSourcesApiClient;
import no.unit.nva.cristin.funding.sources.common.MappingUtils;
import no.unit.nva.cristin.funding.sources.model.nva.FundingSource;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchFundingSourceHandler extends CristinHandler<Void, FundingSource> {

    private static final String CODE_PATH_PARAMETER_NAME = "identifier";
    private final transient CristinFundingSourcesApiClient cristinClient;

    @JacocoGenerated
    public FetchFundingSourceHandler() {
        this(defaultClient(), new Environment());
    }

    public FetchFundingSourceHandler(CristinFundingSourcesApiClient cristinClient, Environment environment) {
        super(Void.class, environment);
        this.cristinClient = cristinClient;
    }

    @Override
    protected FundingSource processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var urlEncodedIdentifier = requestInfo.getPathParameter(CODE_PATH_PARAMETER_NAME);
        var identifier = URLDecoder.decode(urlEncodedIdentifier, StandardCharsets.UTF_8);
        var cristinFundingSource = cristinClient.fetchFundingSource(identifier);

        var domainName = environment.readEnv(ENV_KEY_DOMAIN_NAME);
        var basePath = environment.readEnvOpt(ENV_KEY_BASE_PATH).orElse("");

        return MappingUtils.cristinModelToNvaModel(cristinFundingSource, domainName, basePath);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, FundingSource output) {
        return HttpURLConnection.HTTP_OK;
    }
}
