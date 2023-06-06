package no.unit.nva.cristin.organization.fetch;

import static no.unit.nva.cristin.organization.common.Constants.CLIENT_WANTS_VERSION_OF_THE_API_CLIENT;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_2023_05_26;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_ONE;
import java.util.Map;
import no.unit.nva.cristin.common.client.ClientProvider;
import no.unit.nva.cristin.common.client.IFetchApiClient;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.cristin.organization.common.client.version20230526.CristinOrgApiClient20230526;
import no.unit.nva.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgFetchClientProvider
    implements ClientProvider<IFetchApiClient<Map<String, String>, Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgFetchClientProvider.class);

    @Override
    public IFetchApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        if (VERSION_2023_05_26.equals(apiVersion)) {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_05_26);
            return getVersion20230526();
        }
        logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
        return getVersionOne();
    }

    protected IFetchApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }

    protected IFetchApiClient<Map<String, String>, Organization> getVersion20230526() {
        return new CristinOrgApiClient20230526();
    }
}
