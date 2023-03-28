package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.attempt.Try;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.Utils.isOrcid;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.PERSON_QUERY_CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.utils.UriUtils.PERSON;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.core.attempt.Try.attempt;

public class CristinPersonApiClient extends ApiClient {

    public static final String IDENTITY_NUMBER_PATH = "identityNumber";
    public static final String ERROR_MESSAGE_NO_MATCH_FOUND_FOR_SUPPLIED_PAYLOAD = "No match found for supplied "
            + "payload";

    /**
     * Create CristinPersonApiClient with default HTTP client.
     */
    public CristinPersonApiClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    public CristinPersonApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Creates a SearchResponse based on fetch from Cristin upstream.
     *
     * @param requestQueryParams the query params from the request
     * @return SearchResponse object with hits from upstream and metadata
     * @throws ApiGatewayException if something went wrong
     */
    public SearchResponse<Person> generateQueryResponse(Map<String, String> requestQueryParams)
            throws ApiGatewayException {

        var startRequestTime = System.currentTimeMillis();
        var response = queryPersons(requestQueryParams);
        var cristinPersons = getEnrichedPersonsUsingQueryResponse(response);
        var persons = mapCristinPersonsToNvaPersons(cristinPersons);
        var endRequestTime = System.currentTimeMillis();
        var id = createIdUriFromParams(requestQueryParams, PERSON_PATH_NVA);

        return new SearchResponse<Person>(id)
                .withContext(PERSON_QUERY_CONTEXT)
                .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
                .withHits(persons);
    }

    /**
     * Create a list of CristinPersons from Cristin response.
     *
     * @param response from Cristin API
     * @return List of valid CristinPersons from Response
     * @throws ApiGatewayException when transformation from response fails
     */
    public List<CristinPerson> getEnrichedPersonsUsingQueryResponse(HttpResponse<String> response)
            throws ApiGatewayException {

        var personsFromQuery = asList(getDeserializedResponse(response, CristinPerson[].class));
        var cristinUris = extractCristinUrisFromPersons(personsFromQuery);
        var individualResponses = fetchQueryResultsOneByOne(cristinUris);
        var enrichedCristinPersons = mapResponsesToCristinPersons(individualResponses);

        return allPersonsWereEnriched(personsFromQuery, enrichedCristinPersons)
                ? enrichedCristinPersons
                : combineResultsWithQueryInCaseEnrichmentFails(personsFromQuery, enrichedCristinPersons);
    }

    /**
     * Create a list of CristinPersons from Cristin response for authorized user.
     *
     * @param response from Cristin API
     * @return List of valid CristinPersons from Response
     * @throws ApiGatewayException when transformation from response fails
     */
    public List<CristinPerson> getEnrichedPersonsWithNinUsingQueryResponse(HttpResponse<String> response)
            throws ApiGatewayException {

        var personsFromQuery = asList(getDeserializedResponse(response, CristinPerson[].class));
        var cristinUris = extractCristinUrisFromPersons(personsFromQuery);
        var individualResponses = authorizedFetchQueryResultsOneByOne(cristinUris);
        var enrichedCristinPersons = mapResponsesToCristinPersons(individualResponses);

        return allPersonsWereEnriched(personsFromQuery, enrichedCristinPersons)
                ? enrichedCristinPersons
                : combineResultsWithQueryInCaseEnrichmentFails(personsFromQuery, enrichedCristinPersons);
    }

    protected List<CristinPerson> combineResultsWithQueryInCaseEnrichmentFails(List<CristinPerson> personsFromQuery,
                                                                               List<CristinPerson> enrichedPersons) {
        var enrichedPersonIds = enrichedPersons.stream()
                .map(CristinPerson::getCristinPersonId)
                .collect(Collectors.toSet());

        var missingPersons = personsFromQuery.stream()
                .filter(queryPerson -> !enrichedPersonIds.contains(queryPerson.getCristinPersonId()))
                .collect(Collectors.toList());

        ArrayList<CristinPerson> result = new ArrayList<>();
        result.addAll(enrichedPersons);
        result.addAll(missingPersons);
        return result;
    }

    /**
     * Perform a query for Persons matching criteria in parameters.
     *
     * @param parameters containing search criteria for Cristin Person API
     * @return HttpResponse containing CristinPersons matching search criteria in parameters
     * @throws ApiGatewayException request fails or contains errors
     */
    public HttpResponse<String> queryPersons(Map<String, String> parameters) throws ApiGatewayException {
        var uri = generateQueryPersonsUrl(parameters);
        var response = fetchQueryResults(uri);
        var id = createIdUriFromParams(parameters, PERSON_PATH_NVA);
        checkHttpStatusCode(id, response.statusCode(), response.body());
        return response;
    }

    protected URI generateQueryPersonsUrl(Map<String, String> parameters) {
        var cristinPersonQuery = new CristinPersonQuery()
                .withFromPage(parameters.get(PAGE))
                .withItemsPerPage(parameters.get(NUMBER_OF_RESULTS))
                .withName(parameters.get(NAME));
        if (parameters.containsKey(ORGANIZATION)) {
            cristinPersonQuery = cristinPersonQuery.withOrganization(parameters.get(ORGANIZATION));
        }
        return cristinPersonQuery.toURI();
    }

    /**
     * Creates a SearchResponse based on fetch from Cristin upstream for authorized User.
     *
     * @param requestQueryParameters the query params from the request
     * @return SearchResponse object with hits from upstream and metadata containing NIN
     * @throws ApiGatewayException if something went wrong
     */
    public SearchResponse<Person> authorizedGenerateQueryResponse(Map<String, String> requestQueryParameters)
        throws ApiGatewayException {

        var startRequestTime = System.currentTimeMillis();
        var response = authorizedQueryPersons(requestQueryParameters);
        var cristinPersons = getEnrichedPersonsWithNinUsingQueryResponse(response);
        var persons = authorizedMappingCristinPersonsToNvaPersons(cristinPersons);
        var endRequestTime = System.currentTimeMillis();
        var id = createIdUriFromParams(requestQueryParameters, PERSON_PATH_NVA);

        return new SearchResponse<Person>(id)
                   .withContext(PERSON_QUERY_CONTEXT)
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                   .usingHeadersAndQueryParams(response.headers(), requestQueryParameters)
                   .withHits(persons);
    }

    private HttpResponse<String> authorizedQueryPersons(Map<String, String> parameters)
        throws ApiGatewayException {
        var uri = generateQueryPersonsUrl(parameters);
        var response = fetchGetResultWithAuthentication(uri);
        var id = createIdUriFromParams(parameters, PERSON_PATH_NVA);
        checkHttpStatusCode(id, response.statusCode(), response.body());
        return response;
    }

    private List<URI> extractCristinUrisFromPersons(List<CristinPerson> personsFromQuery) {
        return personsFromQuery.stream()
                .map(CristinPerson::getCristinPersonId)
                .map(CristinPersonQuery::fromId)
                .collect(Collectors.toList());
    }

    private List<CristinPerson> mapResponsesToCristinPersons(List<HttpResponse<String>> responses) {
        return responses.stream()
                .map(attempt(response -> getDeserializedResponse(response, CristinPerson.class)))
                .map(Try::orElseThrow)
                .collect(Collectors.toList());
    }

    private boolean allPersonsWereEnriched(List<CristinPerson> personsFromQuery,
                                           List<CristinPerson> enrichedCristinPersons) {
        return personsFromQuery.size() == enrichedCristinPersons.size();
    }

    public List<Person> mapCristinPersonsToNvaPersons(List<CristinPerson> cristinPersons) {
        return cristinPersons.stream().map(CristinPerson::toPerson).collect(Collectors.toList());
    }

    public List<Person> authorizedMappingCristinPersonsToNvaPersons(List<CristinPerson> cristinPersons) {
        return cristinPersons.stream().map(CristinPerson::toPersonWithAuthorizedFields).collect(Collectors.toList());
    }

    /**
     * Creates a Person object based on what is fetched from Cristin upstream.
     *
     * @param identifier the identifier of the person to fetch
     * @return Person object with person data from upstream
     */
    public Person generateGetResponse(String identifier) throws ApiGatewayException {
        var person = getCristinPerson(identifier).toPerson();
        person.setContext(PERSON_CONTEXT);
        return person;
    }

    /**
     * Creates a Person object based on what is fetched from Cristin upstream for an authorized user.
     *
     * @param identifier the identifier of the person to fetch
     * @return Person object with person data from upstream
     */
    public Person authorizedGenerateGetResponse(String identifier) throws ApiGatewayException {
        var person = getCristinPersonWithAuthentication(identifier).toPersonWithAuthorizedFields();
        person.setContext(PERSON_CONTEXT);
        return person;
    }

    protected CristinPerson getCristinPersonWithAuthentication(String identifier) throws ApiGatewayException {
        var uri = getCorrectUriForIdentifier(identifier);
        var response = fetchGetResultWithAuthentication(uri);
        checkHttpStatusCode(getNvaApiId(identifier, PERSON), response.statusCode(), response.body());
        return getDeserializedResponse(response, CristinPerson.class);
    }

    protected CristinPerson getCristinPerson(String identifier) throws ApiGatewayException {
        var uri = getCorrectUriForIdentifier(identifier);
        var response = fetchGetResult(uri);
        checkHttpStatusCode(getNvaApiId(identifier, PERSON), response.statusCode(), response.body());
        return getDeserializedResponse(response, CristinPerson.class);
    }

    private URI getCorrectUriForIdentifier(String identifier) {
        return isOrcid(identifier) ? CristinPersonQuery.fromOrcid(identifier) : CristinPersonQuery.fromId(identifier);
    }

    /**
     * Perform a query for Person matching National Identification Number in request body.
     *
     * @param nationalIdentificationNumber National Identification Number uniquely identifying person
     * @return Person object with person data from upstream
     * @throws ApiGatewayException when request fails at some point
     */
    public Person getPersonFromNationalIdentityNumber(String nationalIdentificationNumber) throws ApiGatewayException {
        // Upstream uses a query for national id even though it only returns 1 hit
        var cristinPersons = queryUpstreamUsingIdentityNumber(nationalIdentificationNumber);
        throwNotFoundIfNoMatches(cristinPersons);
        var person = enrichFirstMatchFromQueryResponse(cristinPersons).toPersonWithAuthorizedFields();
        person.setContext(PERSON_CONTEXT);
        return person;
    }

    private List<CristinPerson> queryUpstreamUsingIdentityNumber(String identifier) throws ApiGatewayException {
        var queryUri = CristinPersonQuery.fromNationalIdentityNumber(identifier);
        var queryResponse = fetchQueryResults(queryUri);
        checkHttpStatusCode(idUriForIdentityNumber(), queryResponse.statusCode(), queryResponse.body());
        return asList(getDeserializedResponse(queryResponse, CristinPerson[].class));
    }

    protected void throwNotFoundIfNoMatches(List<CristinPerson> cristinPersons) throws NotFoundException {
        if (Objects.isNull(cristinPersons) || cristinPersons.isEmpty()) {
            throw new NotFoundException(ERROR_MESSAGE_NO_MATCH_FOUND_FOR_SUPPLIED_PAYLOAD);
        }
    }

    protected CristinPerson enrichFirstMatchFromQueryResponse(List<CristinPerson> cristinPersons)
            throws ApiGatewayException {

        var fetchUri = extractFirstUriFromListOfCristinPersons(cristinPersons);
        var fetchResponse = fetchGetResult(fetchUri);
        checkHttpStatusCode(idUriForIdentityNumber(), fetchResponse.statusCode(), fetchResponse.body());
        return getDeserializedResponse(fetchResponse, CristinPerson.class);
    }

    private URI extractFirstUriFromListOfCristinPersons(List<CristinPerson> cristinPersons) {
        return cristinPersons.stream().findFirst()
                .map(CristinPerson::getCristinPersonId)
                .map(CristinPersonQuery::fromId).orElseThrow();
    }

    private URI idUriForIdentityNumber() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(IDENTITY_NUMBER_PATH).getUri();
    }
}
