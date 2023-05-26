package no.unit.nva.cristin.organization;

import java.util.Map;
import no.unit.nva.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgQueryClientProvider
    implements IClientProvider<IQueryApiClient<Map<String, String>, Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgQueryClientProvider.class);

    public static final String VERSION = "version";
    public static final String VERSION_ONE = "1";
    public static final String VERSION_2023_05_10 = "2023-05-10";
    public static final String CLIENT_WANTS_VERSION_OF_THE_API_CLIENT = "Client wants version {} of the api client";

    @Override
    public IQueryApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        if (VERSION_2023_05_10.equals(apiVersion)) {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_05_10);
            return getVersionTwo();
        }
        logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
        return getVersionOne();
    }

    public IQueryApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }

    public IQueryApiClient<Map<String, String>, Organization> getVersionTwo() {
        return new CristinOrgApiClientVersion2();
    }
}
