package no.unit.nva.cristin.organization.common.client.version20230526;

import static java.util.Arrays.asList;
import static no.unit.nva.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
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
import no.unit.nva.cristin.common.client.IFetchApiClient;
import no.unit.nva.cristin.common.client.IQueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.version20230526.mapper.OrganizationFromUnitMapper;
import no.unit.nva.cristin.organization.dto.version20230526.UnitDto;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CristinOrgApiClientVersion2 extends ApiClient
    implements IQueryApiClient<Map<String, String>, Organization>,
               IFetchApiClient<Map<String, String>, Organization> {

    public static final URI ORGANIZATION_ID_URI = getNvaApiUri(ORGANIZATION_PATH);

    public CristinOrgApiClientVersion2() {
        this(defaultHttpClient());
    }

    public CristinOrgApiClientVersion2(HttpClient client) {
        super(client);
    }

    /**
     * Fetch Organizations matching given query criteria using version 2 code.
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

    @Override
    public Organization executeFetch(Map<String, String> params) throws ApiGatewayException {
        var fetchUri = getCristinUri(params.get(IDENTIFIER));
        var response = fetchGetResult(fetchUri);
        var organization = getOrganization(response);
        organization.setContext(ORGANIZATION_CONTEXT);

        return organization;
    }

    private URI getCristinUri(String identifier) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                   .addChild(UNITS_PATH)
                   .addChild(identifier)
                   .getUri();
    }

    private Organization getOrganization(HttpResponse<String> response) throws BadGatewayException {
        var unit = getDeserializedResponse(response, UnitDto.class);

        return new OrganizationFromUnitMapper().apply(unit);
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
