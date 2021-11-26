package no.unit.nva.cristin.person.client;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.PERSON_QUERY_CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.attempt.Try;
import nva.commons.core.ioutils.IoUtils;

public class CristinPersonApiClient extends ApiClient {

    private static final String CRISTIN_GET_PERSON_JSON =
        "cristinGetPersonResponse.json";

    public CristinPersonApiClient() {
        this(HttpClient.newHttpClient());
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

    protected List<CristinPerson> getEnrichedPersonsUsingQueryResponse(HttpResponse<String> response)
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

    protected HttpResponse<String> queryPersons(Map<String, String> parameters) throws ApiGatewayException {
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
     * @param id the identifier of the person to fetch
     * @return Person object with person data from upstream
     */
    public Person generateGetResponse(String id) {
        Person person = fetchDummyResponse().toPerson();
        person.setContext(PERSON_CONTEXT);
        return person;
    }

    private CristinPerson fetchDummyResponse() {
        String body = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PERSON_JSON));
        return attempt(() -> OBJECT_MAPPER.readValue(body, CristinPerson.class))
            .orElseThrow(failure -> new RuntimeException("Error reading dummy Json"));
    }

}
