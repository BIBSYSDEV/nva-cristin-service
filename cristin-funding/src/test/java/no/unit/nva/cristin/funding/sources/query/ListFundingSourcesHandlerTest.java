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
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.zalando.problem.Problem;

@WireMockTest(httpsEnabled = true)
public class ListFundingSourcesHandlerTest {
    private static final String COGNITO_AUTHORIZER_URLS = "COGNITO_AUTHORIZER_URLS";

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
        when(environment.readEnv(COGNITO_AUTHORIZER_URLS)).thenReturn("http://localhost:3000");
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

    @ParameterizedTest(name = "Should support accept header {0}")
    @ValueSource(strings = {"application/json", "application/ld+json"})
    public void shouldReturnAllFundingSourcesFromCristinOnSuccess(String acceptHeaderValue) throws IOException {
        try (var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                             .withHeaders(Map.of("Accept", acceptHeaderValue))
                             .build()) {
            cristinFundingSourcesStubs.stubSuccess();
            handlerUnderTest.handleRequest(input, output, context);
        }

        GatewayResponse<FundingSources> response = GatewayResponse.fromOutputStream(output, FundingSources.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var fundingSources = response.getBodyObject(FundingSources.class);

        var expectedContext = URI.create("https://bibsysdev.github.io/src/funding-context.json");
        assertThat(fundingSources.context(), is(equalTo(expectedContext)));

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/cristin/funding-sources");
        assertThat(fundingSources.id(), is(equalTo(expectedId)));

        assertThat(fundingSources.sources(), iterableWithSize(2));

        var firstEntry = fundingSources.sources().get(0);
        assertThat(firstEntry.identifier(), is(equalTo("EC/FP7")));

        var secondEntry = fundingSources.sources().get(1);
        assertThat(secondEntry.identifier(), is(equalTo("EC/H2020")));
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
