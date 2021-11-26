package no.unit.nva.cristin.person.handler;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonQueryHandlerTest {

    private static final String RANDOM_NAME = "John Smith";
    private static final String NVA_API_QUERY_PERSON_JSON =
        "nvaApiQueryPersonResponse.json";

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private PersonQueryHandler handler;

    @BeforeEach
    void setUp() {
        CristinPersonApiClientStub apiClient = new CristinPersonApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new PersonQueryHandler(apiClient, environment);
    }

    @Test
    void shouldReturnResponseWhenCallingEndpointWithNameParameter() throws IOException {
        SearchResponse<Person> actual = sendDefaultQuery().getBodyObject(SearchResponse.class);
        String expectedString = IoUtils.stringFromResources(Path.of(NVA_API_QUERY_PERSON_JSON));
        SearchResponse<Person> expected = OBJECT_MAPPER.readValue(expectedString, SearchResponse.class);

        // Type casting problems when using generic types. Needed to convert. Was somehow converting to LinkedHashMap
        List<Person> expectedPersons = OBJECT_MAPPER.convertValue(expected.getHits(), new TypeReference<>() {});
        List<Person> actualPersons = OBJECT_MAPPER.convertValue(actual.getHits(), new TypeReference<>() {});
        expected.setHits(expectedPersons);
        actual.setHits(actualPersons);

        assertEquals(expected, actual);
    }

    private GatewayResponse<SearchResponse> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(QUERY, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }
}
