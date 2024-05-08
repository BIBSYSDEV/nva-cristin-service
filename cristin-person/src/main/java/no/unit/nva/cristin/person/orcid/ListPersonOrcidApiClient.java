package no.unit.nva.cristin.person.orcid;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.orcid.model.CristinPersonOrcid;
import no.unit.nva.cristin.person.orcid.model.PersonOrcid;
import no.unit.nva.cristin.person.orcid.model.PersonsOrcid;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class ListPersonOrcidApiClient extends ApiClient implements FetchApiClient<Void, PersonsOrcid> {

    public static final String PERSONS_ORCID_PATH = "person/orcid";
    public static final URI PERSONS_ORCID_ID_URI = getNvaApiUri(PERSONS_ORCID_PATH);
    public static final String CRISTIN_PERSONS_ORCID_PATH = "persons/orcid";

    public ListPersonOrcidApiClient() {
        this(defaultHttpClient());
    }

    public ListPersonOrcidApiClient(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public PersonsOrcid executeFetch(Void params) throws ApiGatewayException {
        var queryUri = createCristinQueryUri(emptyMap(), CRISTIN_PERSONS_ORCID_PATH);
        var response = queryUpstream(queryUri);
        var personsOrcid = getPersonsOrcid(response);

        return new PersonsOrcid(personsOrcid.size(), personsOrcid);
    }

    private HttpResponse<String> queryUpstream(URI uri) throws ApiGatewayException {
        var response = fetchGetResultWithAuthentication(uri);
        checkHttpStatusCode(PERSONS_ORCID_ID_URI, response.statusCode(), response.body());

        return response;
    }

    private List<PersonOrcid> getPersonsOrcid(HttpResponse<String> response) throws BadGatewayException {
        var cristinPersonsOrcid = asList(getDeserializedResponse(response, CristinPersonOrcid[].class));

        return cristinPersonsOrcid.stream()
                   .map(CristinPersonOrcid::toPersonOrcid)
                   .collect(Collectors.toList());
    }

}
