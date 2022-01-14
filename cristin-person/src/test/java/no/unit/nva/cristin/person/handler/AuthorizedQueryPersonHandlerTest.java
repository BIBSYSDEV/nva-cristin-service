package no.unit.nva.cristin.person.handler;

public class AuthorizedQueryPersonHandlerTest {

//    private static final String NIN = "nin";
//    private static final String SAMPLE_NIN = "01010101006";
//    private static final String ALPHA_NOT_NIN = "ABCDEFGHIJK";
//    private static final String ILLEGAL_PARAMETER_NAME = "non_nin";
//    public static final String MISSING_FROM_QUERY_PARAMETERS_NIN = "Missing from query parameters: NationalIdentificationNumber";
//
//    private final Environment environment = new Environment();
//    private Context context;
//    private ByteArrayOutputStream output;
//    private AuthorizedQueryPersonHandler handler;
//
//    @BeforeEach
//    void setUp() throws ApiGatewayException {
//        context = mock(Context.class);
//        output = new ByteArrayOutputStream();
//        AuthorizedCristinPersonApiClient authorizedCristinPersonApiClient =
//                mock(AuthorizedCristinPersonApiClient.class);
//        CristinPerson mockPerson = mock(CristinPerson.class);
//        when(authorizedCristinPersonApiClient.getCristinPerson(any())).thenReturn(mockPerson);
//        AuthorizedQueryPersonHandler spyHandler = new AuthorizedQueryPersonHandler(environment);
//        handler = spy(spyHandler);
//        doReturn(authorizedCristinPersonApiClient).when(handler).getAuthorizedCristinPersonApiClient();
//    }
//
//    @Test
//    void shouldReturn200ResponseWhenCallingEndpointWithNationalIdentifierParameter() throws IOException {
//        InputStream input = requestWithQueryParameters(Map.of(NIN, SAMPLE_NIN));
//        handler.handleRequest(input, output, context);
//        GatewayResponse<Person> gatewayResponse = GatewayResponse.fromOutputStream(output);
//        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
//    }
//
//    @Test
//    void shouldReturn400BadRequestWhenCallingEndpointWithIllegalNationalIdentifierParameter() throws IOException {
//        InputStream input = requestWithQueryParameters(Map.of(NIN, ALPHA_NOT_NIN));
//        handler.handleRequest(input, output, context);
//        GatewayResponse<Person> gatewayResponse = GatewayResponse.fromOutputStream(output);
//        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
//        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PARAMETER_FOR_PERSON_ID));
//    }
//
//    @Test
//    void shouldReturn400BadRequestWhenCallingEndpointWithIllegalQueryParameterName() throws IOException {
//        InputStream input = requestWithQueryParameters(Map.of(ILLEGAL_PARAMETER_NAME, SAMPLE_NIN));
//        handler.handleRequest(input, output, context);
//        GatewayResponse<Person> gatewayResponse = GatewayResponse.fromOutputStream(output);
//        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
//        assertThat(gatewayResponse.getBody(), containsString(MISSING_FROM_QUERY_PARAMETERS_NIN));
//
//    }
//
//    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
//        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
//            .withBody(null)
//            .withQueryParameters(map)
//            .build();
//    }

}
