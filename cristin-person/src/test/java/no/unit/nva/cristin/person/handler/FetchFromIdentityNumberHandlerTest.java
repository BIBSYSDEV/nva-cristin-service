package no.unit.nva.cristin.person.handler;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.handler.FetchFromIdentityNumberHandler.NIN_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FetchFromIdentityNumberHandlerTest {

    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    private static final String NVA_API_GET_PERSON_RESPONSE_JSON =
        "nvaApiGetPersonResponse.json";
    private static final String DEFAULT_IDENTITY_NUMBER = "12345612345";
    private static final String VALID_CRISTIN_NATIONAL_ID_URI = "https://api.cristin-test.uio"
        + ".no/v2/persons?national_id=12345612345&lang=en,nb,nn";
    private static final String URI_FIRST_HIT_FROM_CRISTIN = "https://api.cristin.no/v2/persons/359084?lang=en,nb,nn";
    private final Environment environment = new Environment();
    private CristinPersonApiClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private FetchFromIdentityNumberHandler handler;

    @BeforeEach
    void setUp() {
        apiClient = new CristinPersonApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
    }

    @Test
    void shouldReturnPersonWhenMockedHttpResponseContainsCristinPerson() throws Exception {
        Person actual = sendQuery(defaultBody(), EMPTY_MAP).getBodyObject(Person.class);
        String expectedString = IoUtils.stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));
        Person expected = OBJECT_MAPPER.readValue(expectedString, Person.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldProduceCorrectCristinUriFromNationalIdentifier() throws IOException {
        apiClient = spy(apiClient);
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
        sendQuery(defaultBody(), EMPTY_MAP);

        verify(apiClient).fetchQueryResults(new UriWrapper(VALID_CRISTIN_NATIONAL_ID_URI).getUri());
        verify(apiClient).fetchGetResult(new UriWrapper(URI_FIRST_HIT_FROM_CRISTIN).getUri());
    }

    private GatewayResponse<Person> sendQuery(TypedValue body, Map<String, String> queryParams)
        throws IOException {

        InputStream input = requestWithParams(body, queryParams);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithParams(TypedValue body, Map<String, String> queryParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<TypedValue>(OBJECT_MAPPER)
            .withBody(body)
            .withQueryParameters(queryParams)
            .build();
    }

    private TypedValue defaultBody() {
        return new TypedValue(NIN_TYPE, DEFAULT_IDENTITY_NUMBER);
    }
}
