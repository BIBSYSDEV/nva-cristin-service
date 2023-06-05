package no.unit.nva.cristin.organization;

import java.util.Map;
import no.unit.nva.cristin.common.client.ClientProvider;
import no.unit.nva.cristin.common.client.QueryApiClient;
import no.unit.nva.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgQueryClientProvider
    implements ClientProvider<QueryApiClient<Map<String, String>, Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgQueryClientProvider.class);

    public static final String VERSION = "version";
    public static final String VERSION_ONE = "1";
    public static final String VERSION_2023_05_26 = "2023-05-26";
    public static final String CLIENT_WANTS_VERSION_OF_THE_API_CLIENT = "Client wants version {} of the api client";

    @Override
    public QueryApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        if (VERSION_2023_05_26.equals(apiVersion)) {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_05_26);
            return getVersionTwo();
        }
        logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
        return getVersionOne();
    }

    public QueryApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }

    public QueryApiClient<Map<String, String>, Organization> getVersionTwo() {
        return new CristinOrgApiClientVersion2();
    }
}
