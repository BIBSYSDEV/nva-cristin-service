package no.unit.nva.cristin.person.country;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinCountry;
import no.unit.nva.cristin.person.model.nva.Countries;
import no.unit.nva.cristin.person.model.nva.Country;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class ListCountriesApiClient extends ApiClient {

    public static final String COUNTRY_PATH = "country";
    public static final URI COUNTRY_ID_URI = getNvaApiUri(COUNTRY_PATH);
    public static final String CRISTIN_COUNTRIES_PATH = "countries";
    public static final URI COUNTRY_CONTEXT_JSON = URI.create("https://example.org/country-context.json");

    public ListCountriesApiClient() {
        this(defaultHttpClient());
    }

    public ListCountriesApiClient(HttpClient httpClient) {
        super(httpClient);
    }

    public Countries executeRequest() throws ApiGatewayException {
        Map<String, String> params = emptyMap();
        var queryUri = createCristinQueryUri(params, CRISTIN_COUNTRIES_PATH);
        var response = queryUpstream(queryUri);
        var countries = getCountries(response);

        return new Countries(COUNTRY_CONTEXT_JSON,
                             COUNTRY_ID_URI,
                             countries.size(),
                             countries);
    }

    private HttpResponse<String> queryUpstream(URI uri) throws ApiGatewayException {
        var response = fetchQueryResults(uri);
        checkHttpStatusCode(COUNTRY_ID_URI, response.statusCode(), response.body());

        return response;
    }

    private List<Country> getCountries(HttpResponse<String> response) throws BadGatewayException {
        var countries = asList(getDeserializedResponse(response, CristinCountry[].class));

        return countries.stream()
                   .map(CristinCountry::toCountry)
                   .collect(Collectors.toList());
    }

}
