package no.unit.nva.cristin.organization.fetch;

import java.util.Map;
import no.unit.nva.cristin.common.client.IClientProvider;
import no.unit.nva.cristin.common.client.IFetchApiClient;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.model.Organization;

public class DefaultOrgFetchClientProvider
    implements IClientProvider<IFetchApiClient<Map<String, String>, Organization>> {

    @Override
    public IFetchApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        return getVersionOne();
    }

    private IFetchApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }
}
