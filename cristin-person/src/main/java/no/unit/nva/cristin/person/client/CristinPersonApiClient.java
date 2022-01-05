package no.unit.nva.cristin.person.client;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.Utils.isOrcid;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.PERSON_QUERY_CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.utils.UriUtils.PERSON;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.attempt.Try;
import nva.commons.core.paths.UriWrapper;

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

        long startRequestTime = System.currentTimeMillis();
        HttpResponse<String> response = queryPersons(requestQueryParams);
        List<CristinPerson> cristinPersons = getEnrichedPersonsUsingQueryResponse(response);
        List<Person> persons = mapCristinPersonsToNvaPersons(cristinPersons);
        long endRequestTime = System.currentTimeMillis();
        URI id = createIdUriFromParams(requestQueryParams, PERSON_PATH_NVA);

        return new SearchResponse<Person>(id)
            .withContext(PERSON_QUERY_CONTEXT)
            .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
            .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
            .withHits(persons);
    }

    /**
     * Create a list of CristinPersons from Cristin response.
     * @param response from Cristin API
     * @return List of valid CristinPersons from Response
     * @throws ApiGatewayException when transformation from response fails
     */
    public List<CristinPerson> getEnrichedPersonsUsingQueryResponse(HttpResponse<String> response)
        throws ApiGatewayException {

        List<CristinPerson> personsFromQuery = asList(getDeserializedResponse(response, CristinPerson[].class));
        List<URI> cristinUris = extractCristinUrisFromPersons(personsFromQuery);
        List<HttpResponse<String>> individualResponses = fetchQueryResultsOneByOne(cristinUris);
        List<CristinPerson> enrichedCristinPersons = mapResponsesToCristinPersons(individualResponses);

        return allPersonsWereEnriched(personsFromQuery, enrichedCristinPersons)
            ? enrichedCristinPersons
            : combineResultsWithQueryInCaseEnrichmentFails(personsFromQuery, enrichedCristinPersons);
    }

    protected List<CristinPerson> combineResultsWithQueryInCaseEnrichmentFails(List<CristinPerson> personsFromQuery,
                                                                               List<CristinPerson> enrichedPersons) {
        Set<String> enrichedPersonIds = enrichedPersons.stream()
            .map(CristinPerson::getCristinPersonId)
            .collect(Collectors.toSet());

        List<CristinPerson> missingPersons = personsFromQuery.stream()
            .filter(queryPerson -> !enrichedPersonIds.contains(queryPerson.getCristinPersonId()))
            .collect(Collectors.toList());

        ArrayList<CristinPerson> result = new ArrayList<>();
        result.addAll(enrichedPersons);
        result.addAll(missingPersons);
        return result;
    }

    /**
     * Perform a query for Persons matching criteria in parameters.
     * @param parameters containing search criteria for Cristin Person API
     * @return HttpResponse containing CristinPersons matching search criteria in parameters
     * @throws ApiGatewayException request fails or contains errors
     */
    public HttpResponse<String> queryPersons(Map<String, String> parameters) throws ApiGatewayException {
        URI uri = generateQueryPersonsUrl(parameters);
        HttpResponse<String> response = fetchQueryResults(uri);
        URI id = createIdUriFromParams(parameters, PERSON_PATH_NVA);
        checkHttpStatusCode(id, response.statusCode());

        return response;
    }

    protected URI generateQueryPersonsUrl(Map<String, String> parameters) {
        return new CristinPersonQuery()
            .withFromPage(parameters.get(PAGE))
            .withItemsPerPage(parameters.get(NUMBER_OF_RESULTS))
            .withName(parameters.get(QUERY)).toURI();
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
            //.filter(CristinPerson::hasValidContent) // TODO: What is minimum data needed for a Cristin Person?
            .collect(Collectors.toList());
    }

    private boolean allPersonsWereEnriched(List<CristinPerson> personsFromQuery,
                                           List<CristinPerson> enrichedCristinPersons) {
        return personsFromQuery.size() == enrichedCristinPersons.size();
    }

    private List<Person> mapCristinPersonsToNvaPersons(List<CristinPerson> cristinPersons) {
        return cristinPersons.stream().map(CristinPerson::toPerson).collect(Collectors.toList());
    }

    /**
     * Creates a Person object based on what is fetched from Cristin upstream.
     *
     * @param identifier the identifier of the person to fetch
     * @return Person object with person data from upstream
     */
    public Person generateGetResponse(String identifier) throws NotFoundException, BadGatewayException {
        Person person = getCristinPerson(identifier).toPerson();
        person.setContext(PERSON_CONTEXT);
        return person;
    }

    private CristinPerson getCristinPerson(String identifier) throws NotFoundException, BadGatewayException {
        URI uri = getCorrectUriForIdentifier(identifier);
        HttpResponse<String> response = fetchGetResult(uri);
        checkHttpStatusCode(UriUtils.getNvaApiId(identifier, PERSON), response.statusCode());

        return getDeserializedResponse(response, CristinPerson.class);
    }

    private URI getCorrectUriForIdentifier(String identifier) {
        return isOrcid(identifier) ? CristinPersonQuery.fromOrcid(identifier) : CristinPersonQuery.fromId(identifier);
    }

    // TODO: Add authentication when client authentication is in place
    public Person getPersonFromNationalIdentityNumber(String identifier) throws ApiGatewayException {
        // Upstream uses a query for national id even though it only returns 1 hit
        List<CristinPerson> cristinPersons = queryUpstreamUsingIdentityNumber(identifier);
        throwNotFoundIfNoMatches(cristinPersons);
        CristinPerson enrichedCristinPerson = enrichFirstMatchFromQueryResponse(cristinPersons);
        Person person = enrichedCristinPerson.toPerson();
        person.setContext(PERSON_CONTEXT);

        return person;
    }

    private List<CristinPerson> queryUpstreamUsingIdentityNumber(String identifier)
        throws NotFoundException, BadGatewayException {

        URI queryUri = CristinPersonQuery.fromNationalIdentityNumber(identifier);
        HttpResponse<String> queryResponse = fetchQueryResults(queryUri);
        checkHttpStatusCode(idUriForIdentityNumber(), queryResponse.statusCode());

        return asList(getDeserializedResponse(queryResponse, CristinPerson[].class));
    }

    private void throwNotFoundIfNoMatches(List<CristinPerson> cristinPersons) throws NotFoundException {
        if (Objects.isNull(cristinPersons) || cristinPersons.isEmpty()) {
            throw new NotFoundException(ERROR_MESSAGE_NO_MATCH_FOUND_FOR_SUPPLIED_PAYLOAD);
        }
    }

    private CristinPerson enrichFirstMatchFromQueryResponse(List<CristinPerson> cristinPersons)
        throws NotFoundException, BadGatewayException {

        URI fetchUri = extractFirstUriFromListOfCristinPersons(cristinPersons);
        HttpResponse<String> fetchResponse = fetchGetResult(fetchUri);
        checkHttpStatusCode(idUriForIdentityNumber(), fetchResponse.statusCode());

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
