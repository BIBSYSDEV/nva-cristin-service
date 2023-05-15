package no.unit.nva.cristin.organization;

import java.util.Map;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;

public class DefaultOrgFetchProvider implements IClientProvider
                                                    <RequestInfo, IFetchApiClient<Map<String, String>, Organization>> {

    @Override
    public IFetchApiClient<Map<String, String>, Organization> getClient(RequestInfo params) {
        return getVersionOne();
    }

    private IFetchApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }
}
