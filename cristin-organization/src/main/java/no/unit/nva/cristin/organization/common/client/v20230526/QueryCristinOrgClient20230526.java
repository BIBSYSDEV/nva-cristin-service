package no.unit.nva.cristin.organization.common.client.v20230526;

import static java.util.Arrays.asList;
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.organization.common.QueryParamConverter.translateToCristinApi;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.v20230526.mapper.OrganizationFromUnitMapper;
import no.unit.nva.cristin.organization.dto.v20230526.UnitDto;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class QueryCristinOrgClient20230526 extends ApiClient
    implements CristinQueryApiClient<Map<String, String>, Organization> {

    public static final URI ORGANIZATION_ID_URI = getNvaApiUri(ORGANIZATION_PATH);

    public QueryCristinOrgClient20230526() {
        this(defaultHttpClient());
    }

    public QueryCristinOrgClient20230526(HttpClient client) {
        super(client);
    }

    /**
     * Query organizations matching given query criteria. By specifying query param depth one can choose how deep in
     * the organization tree the search should go.
     *
     * @param params Map containing verified query parameters
     */
    @Override
    public SearchResponse<Organization> executeQuery(Map<String, String> params) throws ApiGatewayException {
        var queryUri = createCristinQueryUri(translateToCristinApi(params), UNITS_PATH);
        var start = System.currentTimeMillis();
        var response = queryUpstream(queryUri);
        var organizations = getOrganizations(response);
        var totalProcessingTime = calculateProcessingTime(start, System.currentTimeMillis());

        return new SearchResponse<Organization>(ORGANIZATION_ID_URI)
                   .withContext(ORGANIZATION_CONTEXT)
                   .withHits(organizations)
                   .usingHeadersAndQueryParams(response.headers(), params)
                   .withProcessingTime(totalProcessingTime);
    }

    private HttpResponse<String> queryUpstream(URI uri) throws ApiGatewayException {
        var response = fetchQueryResults(uri);
        checkHttpStatusCode(ORGANIZATION_ID_URI, response.statusCode(), response.body());

        return response;
    }

    private List<Organization> getOrganizations(HttpResponse<String> response) throws BadGatewayException {
        var units = asList(getDeserializedResponse(response, UnitDto[].class));

        return units.stream()
                   .map(new OrganizationFromUnitMapper())
                   .collect(Collectors.toList());
    }

}
