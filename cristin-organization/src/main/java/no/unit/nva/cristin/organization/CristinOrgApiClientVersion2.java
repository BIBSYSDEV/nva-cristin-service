package no.unit.nva.cristin.organization;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.IQueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.OrganizationFromUnitMapper;
import no.unit.nva.cristin.organization.dto.UnitDto;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class CristinOrgApiClientVersion2 extends CristinOrganizationApiClient
    implements IQueryApiClient<Map<String, String>, Organization> {

    public CristinOrgApiClientVersion2() {
        super();
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
        var searchResponse = queryUpstream(queryUri);
        var totalProcessingTime = System.currentTimeMillis() - start;

        return updateSearchResponseMetadata(searchResponse, params, totalProcessingTime);
    }

    private SearchResponse<Organization> queryUpstream(URI uri) throws ApiGatewayException {
        var response = fetchQueryResults(uri);
        var idUri = getNvaApiUri(UNITS_PATH);
        checkHttpStatusCode(idUri, response.statusCode(), response.body());
        var organizations = getOrganizations(response);

        return new SearchResponse<Organization>(idUri)
                   .withHits(organizations)
                   .withSize(getCount(response, organizations));
    }

    private List<Organization> getOrganizations(HttpResponse<String> response) throws BadGatewayException {
        var units = asList(getDeserializedResponse(response, UnitDto[].class));

        return units.stream()
                   .map(new OrganizationFromUnitMapper())
                   .collect(Collectors.toList());
    }

}
