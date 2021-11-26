package no.unit.nva.cristin.person.handler;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonFetchHandlerTest {

    private static final String NVA_API_GET_PERSON_RESPONSE_JSON =
        "nvaApiGetPersonResponse.json";
    private static final Map<String, String> ILLEGAL_PATH_PARAM = Map.of(ID, "string");
    private static final Map<String, String> ILLEGAL_QUERY_PARAMS = Map.of("somekey", "somevalue");
    private static final Map<String, String> VALID_PATH_PARAM = Map.of(ID, "12345");
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private PersonFetchHandler handler;

    @BeforeEach
    void setUp() {
        CristinPersonApiClient apiClient = new CristinPersonApiClient();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new PersonFetchHandler(apiClient, environment);
    }

    @Test
    void shouldReturnResponseWhenCallingEndpointWithNameParameter() throws IOException {
        Person actual = sendQuery(EMPTY_MAP, VALID_PATH_PARAM).getBodyObject(Person.class);
        String expectedString = IoUtils.stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));
        Person expected = OBJECT_MAPPER.readValue(expectedString, Person.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldThrowBadRequestWhenCallingEndpointWithAnyQueryParameters() throws IOException {
        GatewayResponse<Person> gatewayResponse = sendQuery(ILLEGAL_QUERY_PARAMS, VALID_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
            containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP));
    }

    @Test
    void shouldThrowBadRequestWhenPathParamIsNotANumber() throws IOException {
        GatewayResponse<Person> gatewayResponse = sendQuery(EMPTY_MAP, ILLEGAL_PATH_PARAM);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
            containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID));
    }

    private GatewayResponse<Person> sendQuery(Map<String, String> queryParams, Map<String, String> pathParam)
        throws IOException {

        InputStream input = requestWithParams(queryParams, pathParam);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithParams(Map<String, String> queryParams, Map<String, String> pathParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(queryParams)
            .withPathParameters(pathParams)
            .build();
    }

}
