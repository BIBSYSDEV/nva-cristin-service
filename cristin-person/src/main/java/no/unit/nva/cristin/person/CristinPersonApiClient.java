package no.unit.nva.cristin.person;

import static no.unit.nva.cristin.common.model.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.PAGE;
import static no.unit.nva.cristin.common.model.Constants.QUERY;
import static no.unit.nva.cristin.person.Constants.BASE_PATH;
import static no.unit.nva.cristin.person.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.person.Constants.HTTPS;
import static no.unit.nva.cristin.person.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.Constants.PERSON_PATH;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;

@JacocoGenerated // TODO: Dummy response which will be changed to fetch from upstream later
public class CristinPersonApiClient {

    private static final String PERSON_QUERY_CONTEXT = "https://example.org/person-search-context.json";
    private static final String CRISTIN_GET_PERSON_JSON =
        "cristinGetPersonResponse.json";

    /**
     * Creates a SearchResponse based on fetch from Cristin upstream.
     *
     * @param requestQueryParams the query params from the request
     * @return SearchResponse object with hits from upstream and metadata
     * @throws BadRequestException if page requested is out of scope
     */
    public SearchResponse generateQueryResponse(Map<String, String> requestQueryParams) throws BadRequestException {
        return new SearchResponse(getPersonUriWithParams(requestQueryParams))
            .withContext(PERSON_QUERY_CONTEXT)
            .withProcessingTime(1000L)
            .usingHeadersAndQueryParams(dummyHeaders(), requestQueryParams)
            .withHits(getDummyHits());
    }

    private HttpHeaders dummyHeaders() {
        return HttpHeaders.of(Map.of("x-total-count", Collections.singletonList("1")), (s, s2) -> true);
    }

    private List<Person> getDummyHits() {
        CristinPerson cristinPerson = fetchDummyResponse();
        return List.of(cristinPerson.toPerson());
    }

    private CristinPerson fetchDummyResponse() {
        String body = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PERSON_JSON));
        return attempt(() -> OBJECT_MAPPER.readValue(body, CristinPerson.class))
            .orElseThrow(failure -> new RuntimeException("Error reading dummy Json"));
    }

    private URI getPersonUriWithParams(Map<String, String> requestQueryParams) {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild(BASE_PATH).addChild(PERSON_PATH)
            .addQueryParameter(QUERY, requestQueryParams.get(QUERY))
            .addQueryParameter(PAGE, requestQueryParams.get(PAGE))
            .addQueryParameter(NUMBER_OF_RESULTS, requestQueryParams.get(NUMBER_OF_RESULTS))
            .getUri();
    }
}
