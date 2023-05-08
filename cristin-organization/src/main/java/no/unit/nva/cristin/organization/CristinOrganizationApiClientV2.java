package no.unit.nva.cristin.organization;

import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;

public class CristinOrganizationApiClientV2 extends CristinOrganizationApiClient
    implements IQueryApiClient<Organization> {

    @Override
    public SearchResponse<Organization> executeQuery(Map<String, String> queryParams) {
        return null;
    }
}
