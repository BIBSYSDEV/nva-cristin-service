package no.unit.nva.cristin.biobank.fetch;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import com.amazonaws.services.lambda.runtime.Context;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import no.unit.nva.biobank.client.CristinBiobankApiClient;
import no.unit.nva.biobank.fetch.FetchBiobankHandler;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.cristin.biobank.CristinBiobankClientMock;
import no.unit.nva.stubs.FakeContext;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@WireMockTest(httpsEnabled = true)
public class FetchBiobankHandlerTest {

    public static final String APPLICATION_LD_JSON = "application/ld+json";
    public static final String ACCEPT = "Accept";
    private final Context context = new FakeContext();
    private FetchBiobankHandler handlerUnderTest;
    private ByteArrayOutputStream output;
    private final CristinBiobankApiClient biobankClientMock = new CristinBiobankClientMock();

    @BeforeEach
    public void beforeEach(WireMockRuntimeInfo runtimeInfo) {
        this.handlerUnderTest = new FetchBiobankHandler(biobankClientMock, new Environment());
        this.output = new ByteArrayOutputStream();

    }

    @ParameterizedTest(name = "Should fetch biobank with Id {0} ")
    @MethodSource("idAndPayloadProvider")
    public void shouldReturnBiobankWhenExists(String biobankId, String jsonFileName) throws Exception {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(IDENTIFIER, biobankId))
                        .withHeaders(Map.of(ACCEPT, APPLICATION_LD_JSON))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        final var response = GatewayResponse.fromOutputStream(output, Biobank.class);
        var biobank = response.getBodyObject(Biobank.class);
        var expectedId = URI.create("https://api.dev.nva.aws.unit.no/cristin/biobank/" + biobankId);
        var expectedPayload = IoUtils.stringFromResources(Path.of(jsonFileName));

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_OK)));
        assertThat(biobank.getId(), is(equalTo(expectedId)));
        JSONAssert.assertEquals(expectedPayload, biobank.toString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @ParameterizedTest(name = "Should fail fetch biobank with Id {0} ")
    @ValueSource(strings = {"133124"})
    public void shouldReturnFailureWhenBiobankIdDoNotExists(String biobankId) throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(IDENTIFIER, biobankId))
                        .withHeaders(Map.of(ACCEPT, APPLICATION_LD_JSON))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Biobank.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_NOT_FOUND)));
    }

    @ParameterizedTest(name = "Should fail fetch biobank with invalid Id {0} ")
    @ValueSource(strings = { "B3747"})
    public void shouldReturnFailureWhenBiobankIdIsInvalid(String biobankId) throws IOException {
        var input = new HandlerRequestBuilder<Void>(dtoObjectMapper)
                        .withPathParameters(Map.of(IDENTIFIER, biobankId))
                        .withHeaders(Map.of(ACCEPT, APPLICATION_LD_JSON))
                        .build();

        handlerUnderTest.handleRequest(input, output, context);

        var response = GatewayResponse.fromOutputStream(output, Biobank.class);

        assertThat(response.getStatusCode(), is(equalTo(HttpURLConnection.HTTP_BAD_REQUEST)));
    }

    private static Stream<Arguments> idAndPayloadProvider() {
        return Stream.of(
            Arguments.of("2510604", "nvaApiBiobank1.json"),
            Arguments.of("643747", "nvaApiBiobank2.json")
        );
    }

}
