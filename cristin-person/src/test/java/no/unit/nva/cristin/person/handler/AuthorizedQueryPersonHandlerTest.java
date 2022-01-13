package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.cristin.person.client.AuthorizedCristinPersonApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.handler.AuthorizedQueryPersonHandler.ERROR_MESSAGE_INVALID_PARAMETER_FOR_PERSON_ID;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AuthorizedQueryPersonHandlerTest {

    private static final String NIN = "nin";
    private static final String SAMPLE_NIN = "01010101006";
    private static final String ALPHA_NOT_NIN = "ABCDEFGHIJK";
    private static final String ILLEGAL_PARAMETER_NAME = "non_nin";
    public static final String MISSING_FROM_QUERY_PARAMETERS_NIN = "Missing from query parameters: nin";

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private AuthorizedQueryPersonHandler handler;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        AuthorizedCristinPersonApiClient authorizedCristinPersonApiClient =
                mock(AuthorizedCristinPersonApiClient.class);
        CristinPerson mockPerson = mock(CristinPerson.class);
        when(authorizedCristinPersonApiClient.getCristinPerson(any())).thenReturn(mockPerson);
        AuthorizedQueryPersonHandler spyHandler = new AuthorizedQueryPersonHandler(environment);
        handler = spy(spyHandler);
        doReturn(authorizedCristinPersonApiClient).when(handler).getAuthorizedCristinPersonApiClient();
    }

    @Test
    void shouldReturn200ResponseWhenCallingEndpointWithNationalIdentifierParameter() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(NIN, SAMPLE_NIN));
        handler.handleRequest(input, output, context);
        GatewayResponse<Person> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturn500BadRequestWhenCallingEndpointWithIllegalNationalIdentifierParameter() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(NIN, ALPHA_NOT_NIN));
        handler.handleRequest(input, output, context);
        GatewayResponse<Person> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PARAMETER_FOR_PERSON_ID));
    }

    @Test
    void shouldReturn500BadRequestWhenCallingEndpointWithIllegalQueryParameterName() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(ILLEGAL_PARAMETER_NAME, SAMPLE_NIN));
        handler.handleRequest(input, output, context);
        GatewayResponse<Person> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(MISSING_FROM_QUERY_PARAMETERS_NIN));

    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

}
