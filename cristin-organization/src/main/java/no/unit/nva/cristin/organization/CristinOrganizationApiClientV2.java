package no.unit.nva.cristin.organization;

import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;

public class CristinOrganizationApiClientV2 extends CristinOrganizationApiClient
    implements IQueryApiClient<Organization> {

    @Override
    public SearchResponse<Organization> executeQuery(Map<String, String> queryParams) {
        //var queryUri = createCristinQueryUri(translateToCristinApi(queryParams), UNITS_PATH);
        var start = System.currentTimeMillis();
        //var searchResponse = query(queryUri);
        SearchResponse<Organization> searchResponse = new SearchResponse<>(null);
        var totalProcessingTime = System.currentTimeMillis() - start;

        return updateSearchResponseMetadata(searchResponse, queryParams, totalProcessingTime);
    }
}
