package no.unit.nva.cristin.funding.sources.fetch;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_ALLOWED_ORIGIN;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_BASE_PATH;
import static no.unit.nva.cristin.funding.sources.common.EnvironmentKeys.ENV_KEY_DOMAIN_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.isNotNull;
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
import no.unit.nva.cristin.funding.sources.model.nva.FundingSource;
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
public class FetchFundingSourcesHandlerTest {

    private static final String EXISTING_FUNDING_SOURCE_IDENTIFIER = "EC/FP7";
    private static final String EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED = "EC%2FFP7";

    private final Context context = new FakeContext();
    private FetchFundingSourceHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private CristinFundingSourcesStubs cristinFundingSourcesStubs;
    private Environment environment;

    @BeforeEach
    public void beforeEach(WireMockRuntimeInfo runtimeInfo) {
        this.environment = Mockito.mock(Environment.class);

        when(environment.readEnv(ENV_KEY_ALLOWED_ORIGIN)).thenReturn("*");
        when(environment.readEnv(ENV_KEY_DOMAIN_NAME)).thenReturn("https://api.sandbox.nva.aws.unit.no");
        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.of("cristin"));

        var cristinClient = new CristinFundingSourcesApiClient(WiremockHttpClient.create(),
                                                               URI.create(runtimeInfo.getHttpBaseUrl()));
        this.handlerUnderTest = new FetchFundingSourceHandler(cristinClient, environment);
        this.output = new ByteArrayOutputStream();

        cristinFundingSourcesStubs = new CristinFundingSourcesStubs();
    }

    @AfterEach
    public void reset() {
        cristinFundingSourcesStubs.resetStub();
    }

    @ParameterizedTest(name = "Should support accept header {0}")
    @ValueSource(strings = {"application/json", "application/ld+json"})
    public void shouldReturnFundingSourceWhenExists(String acceptHeaderValue) throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED))
                        .withHeaders(Map.of("Accept", acceptHeaderValue))
                        .build();

        cristinFundingSourcesStubs.stubSuccess();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FundingSource.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var fundingSource = response.getBodyObject(FundingSource.class);

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/cristin/funding-sources/"
                                    + EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED);
        assertThat(fundingSource.getContext(), is(not(nullValue())));
        assertThat(fundingSource.getId(), is(equalTo(expectedId)));
        assertThat(fundingSource.getIdentifier(), is(equalTo(EXISTING_FUNDING_SOURCE_IDENTIFIER)));

        assertNamesArePresentForFundingSource(fundingSource);
    }

    @Test
    public void shouldReturnStatusCodeNotFoundWhenFundingSourceDoesNotExistInCristin() throws IOException {
        var urlEncodedIdentifier = "NotFound";
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier", urlEncodedIdentifier))
                        .build();

        cristinFundingSourcesStubs.stubSuccess();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));
    }

    @Test
    public void shouldReturnBadGatewayWhenCristinReturnsMalformedJson() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED))
                        .build();

        cristinFundingSourcesStubs.stubMalformedJsonInResponse();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    public void shouldReturnBadGatewayWhenCristinIsUnavailable() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED))
                        .build();

        var cristinClient = new CristinFundingSourcesApiClient(WiremockHttpClient.create(),
                                                               URI.create("https://localhost:9999"));
        this.handlerUnderTest = new FetchFundingSourceHandler(cristinClient, environment);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    public void shouldUseProvidedBasePathForResourceIds() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED))
                        .build();

        cristinFundingSourcesStubs.stubSuccess();

        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.of("alternate-base-path"));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FundingSource.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var fundingSource = response.getBodyObject(FundingSource.class);

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/alternate-base-path/funding-sources/"
                                    + EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED);
        assertThat(fundingSource.getId(), is(equalTo(expectedId)));
    }

    @Test
    public void shouldSupportEmptyBasePathInResourceIds() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED))
                        .build();

        cristinFundingSourcesStubs.stubSuccess();

        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.empty());

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, FundingSource.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var fundingSource = response.getBodyObject(FundingSource.class);

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/funding-sources/"
                                    + EXISTING_FUNDING_SOURCE_IDENTIFIER_URL_ENCODED);
        assertThat(fundingSource.getId(), is(equalTo(expectedId)));
    }

    private void assertNamesArePresentForFundingSource(FundingSource fundingSource) {
        var expectedNumberOfLanguagesInName = 3;
        assertThat(fundingSource.getLabels(), aMapWithSize(expectedNumberOfLanguagesInName));
        assertThat(fundingSource.getLabels(), hasEntry(equalTo("en"),
                                                     equalTo(EXISTING_FUNDING_SOURCE_IDENTIFIER)));
        assertThat(fundingSource.getLabels(), hasEntry(equalTo("nn"),
                                                     equalTo(EXISTING_FUNDING_SOURCE_IDENTIFIER)));
        assertThat(fundingSource.getLabels(), hasEntry(equalTo("nb"),
                                                     equalTo(EXISTING_FUNDING_SOURCE_IDENTIFIER)));
    }
}
