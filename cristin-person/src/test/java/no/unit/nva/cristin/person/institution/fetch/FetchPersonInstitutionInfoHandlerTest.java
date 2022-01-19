package no.unit.nva.cristin.person.institution.fetch;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.person.institution.fetch.FetchPersonInstitutionInfoHandler.INVALID_ORGANIZATION_ID;
import static no.unit.nva.cristin.person.institution.fetch.FetchPersonInstitutionInfoHandler.INVALID_PERSON_ID;
import static no.unit.nva.cristin.person.institution.fetch.FetchPersonInstitutionInfoHandler.ORG_ID;
import static no.unit.nva.cristin.person.institution.fetch.FetchPersonInstitutionInfoHandler.PERSON_ID;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Map;
import no.unit.nva.cristin.person.model.cristin.CristinPersonInstitutionInfo;
import no.unit.nva.cristin.person.model.nva.PersonInstitutionInfo;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FetchPersonInstitutionInfoHandlerTest {

    private static final String EMPTY_JSON = "{}";
    private static final String VALID_PERSON_ID = "12345";
    private static final String VALID_UNIT_ID = "185.90.0.0";
    private static final String VALID_INSTITUTION_ID = "185";
    private static final String EXPECTED_CRISTIN_URI =
        "https://api.cristin-test.uio.no/v2/persons/12345/institutions/185";
    private static final String INVALID_PATH_PARAM = "abcdef";
    private static final String DUMMY_EMAIL = "email@example.org";
    private static final String DUMMY_PHONE = "+4799112233";
    private static final URI EXPECTED_ID_URI = getExpectedId();

    private final HttpClient clientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private FetchPersonInstitutionInfoClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private FetchPersonInstitutionInfoHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 200));
        apiClient = new FetchPersonInstitutionInfoClient(clientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchPersonInstitutionInfoHandler(apiClient, environment);
    }

    @Test
    void shouldReturnCorrectDataWhenGeneratingResponse() throws IOException, InterruptedException {
        CristinPersonInstitutionInfo cristinInfo = new CristinPersonInstitutionInfo();
        cristinInfo.setEmail(DUMMY_EMAIL);
        cristinInfo.setPhone(DUMMY_PHONE);
        String responseBody = OBJECT_MAPPER.writeValueAsString(cristinInfo);

        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseBody, 200));
        apiClient = new FetchPersonInstitutionInfoClient(clientMock);
        handler = new FetchPersonInstitutionInfoHandler(apiClient, environment);

        GatewayResponse<PersonInstitutionInfo> gatewayResponse =
            sendQuery(null, Map.of(PERSON_ID, VALID_PERSON_ID, ORG_ID, VALID_INSTITUTION_ID));

        PersonInstitutionInfo actual = gatewayResponse.getBodyObject(PersonInstitutionInfo.class);
        PersonInstitutionInfo expected = cristinInfo.toPersonInstitutionInfo(EXPECTED_ID_URI);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldGenerateCorrectCristinUriFromPathParams() throws ApiGatewayException, IOException {
        apiClient = spy(apiClient);
        handler = new FetchPersonInstitutionInfoHandler(apiClient, environment);
        sendQuery(null, Map.of(PERSON_ID, VALID_PERSON_ID, ORG_ID, VALID_INSTITUTION_ID));
        verify(apiClient).fetchGetResult(getCristinUriFromIdentifiers());
    }

    @Test
    void shouldParseUnitIdCorrectlyFromPathParam() throws ApiGatewayException, IOException {
        apiClient = spy(apiClient);
        handler = new FetchPersonInstitutionInfoHandler(apiClient, environment);
        sendQuery(null, Map.of(PERSON_ID, VALID_PERSON_ID, ORG_ID, VALID_UNIT_ID));
        verify(apiClient).fetchGetResult(getCristinUriFromIdentifiers());
    }

    @Test
    void shouldThrowBadRequestOnInvalidPersonIdPathParam() throws IOException {
        GatewayResponse<PersonInstitutionInfo> gatewayResponse =
            sendQuery(null, Map.of(PERSON_ID, INVALID_PATH_PARAM, ORG_ID, VALID_INSTITUTION_ID));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(FetchPersonInstitutionInfoHandler.INVALID_PERSON_ID));
    }

    @Test
    void shouldThrowBadRequestOnInvalidOrganizationPathParam() throws IOException {
        GatewayResponse<PersonInstitutionInfo> gatewayResponse =
            sendQuery(null, Map.of(PERSON_ID, VALID_PERSON_ID, ORG_ID, INVALID_PATH_PARAM));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(),
            containsString(FetchPersonInstitutionInfoHandler.INVALID_ORGANIZATION_ID));
    }

    @Test
    void shouldThrowBadRequestOnMissingPersonPathParam() throws IOException {
        GatewayResponse<PersonInstitutionInfo> gatewayResponse =
            sendQuery(null, Map.of(ORG_ID, VALID_INSTITUTION_ID));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(INVALID_PERSON_ID));
    }

    @Test
    void shouldThrowBadRequestOnMissingOrganizationPathParam() throws IOException {
        GatewayResponse<PersonInstitutionInfo> gatewayResponse =
            sendQuery(null, Map.of(PERSON_ID, VALID_PERSON_ID));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(INVALID_ORGANIZATION_ID));
    }

    private URI getCristinUriFromIdentifiers() {
        return URI.create(EXPECTED_CRISTIN_URI);
    }

    private GatewayResponse<PersonInstitutionInfo> sendQuery(Map<String, String> queryParams,
                                                             Map<String, String> pathParam)
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

    private static URI getExpectedId() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH)
            .addChild(PERSON_PATH_NVA).addChild(VALID_PERSON_ID)
            .addChild(ORGANIZATION_PATH).addChild(VALID_INSTITUTION_ID)
            .getUri();
    }
}
