package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.handler.CreateCristinPersonHandler.DUMMY_FIRST_NAME;
import static no.unit.nva.cristin.person.handler.CreateCristinPersonHandler.DUMMY_NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.handler.CreateCristinPersonHandler.INVALID_PAYLOAD;
import static no.unit.nva.cristin.person.handler.CreateCristinPersonHandler.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.FIRST_NAME;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class CreateCristinPersonHandlerTest {

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private CreateCristinPersonHandler handler;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new CreateCristinPersonHandler(environment);
    }

    @Test
    void shouldReturnDummyPersonWhenRequestHasDummyNationalIdentityNumber() throws Exception {
        Person person = new Person.Builder()
            .withIdentifiers(Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DUMMY_NATIONAL_IDENTITY_NUMBER)))
            .build();
        Person actual = sendQuery(person, Map.of()).getBodyObject(Person.class);
        Person expected = CreateCristinPersonHandler.dummyPerson();

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldReturnBadRequestWhenRequestHasInvalidData() throws Exception {
        GatewayResponse<Person> gatewayResponse = sendQuery(null, Map.of());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(INVALID_PAYLOAD));

        Person personWithMissingIdentity = new Person.Builder()
            .withNames(Set.of(new TypedValue(FIRST_NAME, DUMMY_FIRST_NAME))).build();

        gatewayResponse = sendQuery(personWithMissingIdentity, Map.of());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(INVALID_PAYLOAD));
    }

    private GatewayResponse<Person> sendQuery(Person body, Map<String, String> queryParams) throws IOException {
        InputStream input = requestWithParams(body, queryParams);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithParams(Person body, Map<String, String> queryParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Person>(OBJECT_MAPPER)
            .withBody(body)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withQueryParameters(queryParams)
            .build();
    }
}
