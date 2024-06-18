package no.unit.nva.cristin.funding.sources.query;

import static no.unit.nva.cristin.funding.sources.client.CristinFundingSourcesApiClient.defaultClient;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_BASE_PATH;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_DOMAIN_NAME;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.funding.sources.client.CristinFundingSourcesApiClient;
import no.unit.nva.cristin.funding.sources.common.DomainUriUtils;
import no.unit.nva.cristin.funding.sources.common.MappingUtils;
import no.unit.nva.cristin.funding.sources.model.nva.FundingSources;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

public class ListFundingSourcesHandler extends CristinQueryHandler<Void, FundingSources> {

    private static final URI CONTEXT_URI = UriWrapper.fromUri("https://bibsysdev.github.io/src/funding-context.json")
                                               .getUri();
    private final transient CristinFundingSourcesApiClient cristinClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public ListFundingSourcesHandler() {
        this(defaultClient(), new Environment());
    }

    public ListFundingSourcesHandler(CristinFundingSourcesApiClient cristinClient, Environment environment) {
        super(Void.class, environment);
        this.cristinClient = cristinClient;
    }

    @Override
    protected FundingSources processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var domainName = environment.readEnv(ENV_KEY_DOMAIN_NAME);
        var basePath = environment.readEnvOpt(ENV_KEY_BASE_PATH).orElse(EMPTY_STRING);

        var sources = cristinClient.queryFundingSources()
                          .stream()
                          .map(cfs -> MappingUtils.cristinModelToNvaModel(cfs, domainName, basePath))
                          .collect(Collectors.toList());
        var resultId = DomainUriUtils.getFundingSourcesUri(domainName, basePath);

        return new FundingSources(CONTEXT_URI, resultId, sources);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, FundingSources output) {
        return HttpURLConnection.HTTP_OK;
    }
}
