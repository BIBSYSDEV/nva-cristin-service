package no.unit.nva.cristin.biobank.fetch;

import static no.unit.nva.biobank.common.EnvironmentKeys.ENV_KEY_ALLOWED_ORIGIN;
import static no.unit.nva.biobank.common.EnvironmentKeys.ENV_KEY_BASE_PATH;
import static no.unit.nva.biobank.common.EnvironmentKeys.ENV_KEY_DOMAIN_NAME;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
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
import no.unit.nva.biobank.client.CristinBiobankApiClient;
import no.unit.nva.biobank.fetch.FetchBiobankHandler;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.cristin.biobank.CristinBiobankStubs;
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
public class FetchBiobankHandlerTest {

    private static final String EXISTING_BIOBANK_IDENTIFIER = "EC/FP7";
    private static final String EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED = "EC%2FFP7";

    private final Context context = new FakeContext();
    private FetchBiobankHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private CristinBiobankStubs cristinBiobankStubs;
    private Environment environment;

    @BeforeEach
    public void beforeEach(WireMockRuntimeInfo runtimeInfo) {
        this.environment = Mockito.mock(Environment.class);

        when(environment.readEnv(ENV_KEY_ALLOWED_ORIGIN)).thenReturn("*");
        when(environment.readEnv(ENV_KEY_DOMAIN_NAME)).thenReturn("https://api.sandbox.nva.aws.unit.no");
        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.of("cristin"));

        var cristinClient = new CristinBiobankApiClient(WiremockHttpClient.create(),
                                                        URI.create(runtimeInfo.getHttpBaseUrl()));
        this.handlerUnderTest = new FetchBiobankHandler(cristinClient, environment);
        this.output = new ByteArrayOutputStream();

        cristinBiobankStubs = new CristinBiobankStubs();
    }

    @AfterEach
    public void reset() {
        cristinBiobankStubs.resetStub();
    }

    @ParameterizedTest(name = "Should support accept header {0}")
    @ValueSource(strings = {"application/json", "application/ld+json"})
    public void shouldReturnBiobankWhenExists(String acceptHeaderValue) throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED))
                        .withHeaders(Map.of("Accept", acceptHeaderValue))
                        .build();

        cristinBiobankStubs.stubSuccess();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Biobank.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var biobank = response.getBodyObject(Biobank.class);

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/cristin/biobank/"
                                    + EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED);
        assertThat(biobank.getId(), is(equalTo(expectedId)));
//        assertThat(biobank.getIdentifier(), is(equalTo(EXISTING_FUNDING_SOURCE_IDENTIFIER)));

        assertNamesArePresentForBiobank(biobank);
    }

    @Test
    public void shouldReturnStatusCodeNotFoundWhenBiobankDoesNotExistInCristin() throws IOException {
        var urlEncodedIdentifier = "NotFound";
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier", urlEncodedIdentifier))
                        .build();

        cristinBiobankStubs.stubSuccess();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));
    }

    @Test
    public void shouldReturnBadGatewayWhenCristinReturnsMalformedJson() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED))
                        .build();

        cristinBiobankStubs.stubMalformedJsonInResponse();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    public void shouldReturnBadGatewayWhenCristinIsUnavailable() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED))
                        .build();

        var cristinClient = new CristinBiobankApiClient(WiremockHttpClient.create(),
                                                               URI.create("https://localhost:9999"));
        this.handlerUnderTest = new FetchBiobankHandler(cristinClient, environment);

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Problem.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_GATEWAY)));
    }

    @Test
    public void shouldUseProvidedBasePathForResourceIds() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED))
                        .build();

        cristinBiobankStubs.stubSuccess();

        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.of("alternate-base-path"));

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Biobank.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var biobank = response.getBodyObject(Biobank.class);

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/alternate-base-path/biobank/"
                                    + EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED);
        assertThat(biobank.getId(), is(equalTo(expectedId)));
    }

    @Test
    public void shouldSupportEmptyBasePathInResourceIds() throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of("identifier",
                                                   EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED))
                        .build();

        cristinBiobankStubs.stubSuccess();

        when(environment.readEnvOpt(ENV_KEY_BASE_PATH)).thenReturn(Optional.empty());

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Biobank.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));

        var biobank = response.getBodyObject(Biobank.class);

        var expectedId = URI.create("https://api.sandbox.nva.aws.unit.no/biobank/"
                                    + EXISTING_BIOBANK_IDENTIFIER_URL_ENCODED);
        assertThat(biobank.getId(), is(equalTo(expectedId)));
    }

    private void assertNamesArePresentForBiobank(Biobank fundingSource) {
        var expectedNumberOfLanguagesInName = 3;
        assertThat(fundingSource.getName(), aMapWithSize(expectedNumberOfLanguagesInName));
        assertThat(fundingSource.getName(), hasEntry(equalTo("en"),
                                                     equalTo(EXISTING_BIOBANK_IDENTIFIER)));
        assertThat(fundingSource.getName(), hasEntry(equalTo("nn"),
                                                     equalTo(EXISTING_BIOBANK_IDENTIFIER)));
        assertThat(fundingSource.getName(), hasEntry(equalTo("nb"),
                                                     equalTo(EXISTING_BIOBANK_IDENTIFIER)));
    }
}
