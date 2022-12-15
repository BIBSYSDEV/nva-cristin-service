package no.unit.nva.cristin.funding.sources.query;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_ALLOWED_ORIGIN;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_BASE_PATH;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_DOMAIN_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import no.unit.nva.cristin.funding.sources.CristinFundingSourcesStubs;
import no.unit.nva.cristin.funding.sources.client.CristinFundingSourcesApiClient;
import no.unit.nva.cristin.funding.sources.model.nva.FundingSources;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.stubs.WiremockHttpClient;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public class ListFundingSourcesHandlerTest {

    private final Context context = new FakeContext();
    private ListFundingSourcesHandler handlerUnderTest;
    private Environment environment;
    private ByteArrayOutputStream output;
    private CristinFundingSourcesStubs cristinFundingSourcesStubs;

    @BeforeEach
    public void beforeEach(WireMockRuntimeInfo runtimeInfo) {
        this.environment = Mockito.mock(Environment.class);

        when(environment.readEnv(ENV_KEY_ALLOWED_ORIGIN)).thenReturn("*");
        when(environment.readEnv(ENV_KEY_DOMAIN_NAME)).thenReturn("https://api.sandbox.nva.aws.unit.no");
        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.of("cristin"));

        var cristinClient = new CristinFundingSourcesApiClient(WiremockHttpClient.create(),
                                                               URI.create(runtimeInfo.getHttpBaseUrl()));
        this.handlerUnderTest = new ListFundingSourcesHandler(cristinClient, environment);
        this.output = new ByteArrayOutputStream();
        cristinFundingSourcesStubs = new CristinFundingSourcesStubs();
    }

    @AfterEach
    public void reset() {
        cristinFundingSourcesStubs.resetStub();
    }

    @Test
    public void shouldReturnAllFundingSourcesFromCristinOnSuccess() throws IOException {
        InputStream input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                                .build();

        cristinFundingSourcesStubs.stubSuccess();

        handlerUnderTest.handleRequest(input, output, context);

        GatewayResponse<FundingSources> response = GatewayResponse.fromOutputStream(output, FundingSources.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var fundingSources = response.getBodyObject(FundingSources.class);

        var expectedContext = URI.create("https://bibsysdev.github.io/src/funding-context.json");
        assertThat(fundingSources.getContext(), is(equalTo(expectedContext)));

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/cristin/funding-sources");
        assertThat(fundingSources.getId(), is(equalTo(expectedId)));

        assertThat(fundingSources.getSources(), iterableWithSize(559));
    }

    @Test
    public void shouldReturnBadGatewayWhenCristinReturnsMalformedJson() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .build();

        cristinFundingSourcesStubs.stubMalformedJsonInResponse();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    public void shouldReturnBadGatewayWhenCristinIsUnavailable() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .build();

        var cristinClient = new CristinFundingSourcesApiClient(WiremockHttpClient.create(),
                                                               URI.create("https://localhost:9999"));
        this.handlerUnderTest = new ListFundingSourcesHandler(cristinClient, environment);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

}
