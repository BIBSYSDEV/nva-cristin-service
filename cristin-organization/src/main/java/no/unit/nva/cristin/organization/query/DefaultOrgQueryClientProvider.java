package no.unit.nva.cristin.organization.query;

import static no.unit.nva.cristin.organization.common.Constants.CLIENT_WANTS_VERSION_OF_THE_API_CLIENT;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_2023_05_26;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_ONE;
import java.util.Map;
import no.unit.nva.cristin.common.client.ClientProvider;
import no.unit.nva.cristin.common.client.QueryApiClient;
import no.unit.nva.cristin.organization.common.client.v20230526.QueryCristinOrgClient20230526;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgQueryClientProvider
    implements ClientProvider<QueryApiClient<Map<String, String>, Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgQueryClientProvider.class);

    @Override
    public QueryApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        if (VERSION_2023_05_26.equals(apiVersion)) {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_05_26);
            return getVersion20230526();
        }
        logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
        return getVersionOne();
    }

    public QueryApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }

    public QueryApiClient<Map<String, String>, Organization> getVersion20230526() {
        return new QueryCristinOrgClient20230526();
    }
}
