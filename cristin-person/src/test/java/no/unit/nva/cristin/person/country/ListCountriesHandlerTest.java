package no.unit.nva.cristin.person.country;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import no.unit.nva.cristin.person.model.nva.Countries;
import no.unit.nva.cristin.person.model.nva.Country;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListCountriesHandlerTest {

    public static final String CRISTIN_COUNTRIES_RESPONSE_JSON = "cristinListCountriesResponse.json";
    public static final String EXPECTED_CRISTIN_URI_WITH_DEFAULT_PARAMS =
        "https://api.cristin-test.uio.no/v2/countries";
    public static final String NVA_COUNTRIES_RESPONSE_JSON = "nvaApiListCountriesResponse.json";
    public static final int THREE_HITS = 3;
    public static final URI EXPECTED_CONTEXT = URI.create("https://example.org/country-context.json");
    public static final URI EXPECTED_ID = URI.create("https://api.dev.nva.aws.unit.no/cristin/country");

    private ListCountriesApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private ListCountriesHandler handler;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        var httpClient = mock(HttpClient.class);
        apiClient = new ListCountriesApiClient(httpClient);
        apiClient = spy(apiClient);
        var fakeCristinResponse = IoUtils.stringFromResources(Path.of(CRISTIN_COUNTRIES_RESPONSE_JSON));
        doReturn(new HttpResponseFaker(fakeCristinResponse)).when(apiClient).fetchQueryResults(any());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new ListCountriesHandler(environment, apiClient);
    }

    @Test
    void shouldReturnListOfCountriesWhenCallingUpstream() throws Exception {
        final var response = sendQuery();
        final var responseBody = response.getBodyObject(Countries.class);
        final var expected = IoUtils.stringFromResources(Path.of(NVA_COUNTRIES_RESPONSE_JSON));
        final var expectedList = asList(OBJECT_MAPPER.readValue(expected, Country[].class));

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody.countries().size(), equalTo(THREE_HITS));
        assertThat(responseBody.context(), equalTo(EXPECTED_CONTEXT));
        assertThat(responseBody.id(), equalTo(EXPECTED_ID));
        assertThat(responseBody.countries(), equalTo(expectedList));
    }

    @Test
    void shouldCallCorrectUpstreamUri() throws Exception {
        sendQuery();

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_DEFAULT_PARAMS).getUri());
    }

    private GatewayResponse<Countries> sendQuery() throws IOException {
        var input = generateRequest();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Countries.class);
    }

    private InputStream generateRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .build();
    }

}
