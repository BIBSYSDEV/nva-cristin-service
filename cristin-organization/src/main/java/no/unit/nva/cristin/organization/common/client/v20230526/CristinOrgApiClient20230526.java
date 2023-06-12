package no.unit.nva.cristin.organization.common.client.v20230526;

import static java.util.Arrays.asList;
import static no.unit.nva.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PARENT_UNIT_ID;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
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
import no.unit.nva.cristin.common.client.FetchApiClient;
import no.unit.nva.cristin.common.client.QueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.v20230526.mapper.OrganizationFromUnitMapper;
import no.unit.nva.cristin.organization.dto.v20230526.UnitDto;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CristinOrgApiClient20230526 extends ApiClient
    implements QueryApiClient<Map<String, String>, Organization>,
               FetchApiClient<Map<String, String>, Organization> {

    public static final URI ORGANIZATION_ID_URI = getNvaApiUri(ORGANIZATION_PATH);
    public static final String ALL_RESULTS = "2000";

    public CristinOrgApiClient20230526() {
        this(defaultHttpClient());
    }

    public CristinOrgApiClient20230526(HttpClient client) {
        super(client);
    }

    /**
     * Query organizations matching given query criteria.
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

    /**
     * Fetch one organization matching given query criteria.
     *
     * @param params Map containing verified query parameters depth and identifier
     */
    @Override
    public Organization executeFetch(Map<String, String> params) throws ApiGatewayException {
        var identifier = params.get(IDENTIFIER);
        var fetchUri = getCristinUri(identifier);
        var response = fetchGetResult(fetchUri);
        if (wantsDepth(params)) {
            var fetchSubsUri = createCristinQueryUri(translateParamsForSubUnits(identifier), UNITS_PATH);
            var responseWithSubs = fetchGetResult(fetchSubsUri);
            var organization = getOrganization(response, responseWithSubs);
            organization.setContext(ORGANIZATION_CONTEXT);

            return organization;
        } else {
            var organization = getSingleLevelOrganization(response);
            organization.setContext(ORGANIZATION_CONTEXT);

            return organization;
        }
    }

    private boolean wantsDepth(Map<String, String> params) {
        var depth = params.get(DEPTH);
        return !NONE.equals(depth);
    }

    private URI getCristinUri(String identifier) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                   .addChild(UNITS_PATH)
                   .addChild(identifier)
                   .getUri();
    }

    private Map<String, String> translateParamsForSubUnits(String identifier) {
        return Map.of(PARENT_UNIT_ID, identifier, CRISTIN_PER_PAGE_PARAM, ALL_RESULTS);
    }

    private Organization getSingleLevelOrganization(HttpResponse<String> response) throws BadGatewayException {
        var unit = getDeserializedResponse(response, UnitDto.class);

        return new OrganizationFromUnitMapper().apply(unit);
    }

    private Organization getOrganization(HttpResponse<String> response, HttpResponse<String> responseWithSubs)
        throws BadGatewayException {
        var unit = getDeserializedResponse(response, UnitDto.class);
        var subUnits = List.of(getDeserializedResponse(responseWithSubs, UnitDto[].class));
        unit.setSubUnits(subUnits);

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
