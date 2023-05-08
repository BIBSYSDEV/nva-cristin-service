package no.unit.nva.cristin.organization;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.OrganizationMapper;
import no.unit.nva.cristin.organization.dto.UnitDto;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class CristinOrganizationApiClientV2 extends CristinOrganizationApiClient
    implements IQueryApiClient<Organization> {

    @Override
    public SearchResponse<Organization> executeQuery(Map<String, String> queryParams) throws ApiGatewayException {
        var queryUri = createCristinQueryUri(translateToCristinApi(queryParams), UNITS_PATH);
        var start = System.currentTimeMillis();
        var searchResponse = queryUpstream(queryUri);
        var totalProcessingTime = System.currentTimeMillis() - start;

        return updateSearchResponseMetadata(searchResponse, queryParams, totalProcessingTime);
    }

    protected SearchResponse<Organization> queryUpstream(URI uri) throws ApiGatewayException {
        var response = sendRequestMultipleTimes(uri).get();
        var idUri = getNvaApiUri(UNITS_PATH);
        checkHttpStatusCode(idUri, response.statusCode(), response.body());
        var organizations = getOrganizations(response);

        return new SearchResponse<Organization>(idUri)
                   .withHits(organizations)
                   .withSize(getCount(response, organizations));
    }

    private List<Organization> getOrganizations(HttpResponse<String> response) throws BadGatewayException {
        //List<UnitDto> units = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {});
        var units = asList(getDeserializedResponse(response, UnitDto[].class));

        return units.stream()
                   .map(new OrganizationMapper())
                   .collect(Collectors.toList());
    }

}
