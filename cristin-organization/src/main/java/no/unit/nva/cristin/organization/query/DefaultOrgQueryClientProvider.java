package no.unit.nva.cristin.organization.query;

import static no.unit.nva.cristin.organization.common.Constants.CLIENT_WANTS_VERSION_OF_THE_API_CLIENT;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_2023_05_26;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_ONE;
import java.util.Map;
import no.unit.nva.cristin.common.client.IClientProvider;
import no.unit.nva.cristin.common.client.IQueryApiClient;
import no.unit.nva.cristin.organization.common.client.version20230526.CristinOrgApiClientVersion2;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgQueryClientProvider
    implements IClientProvider<IQueryApiClient<Map<String, String>, Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgQueryClientProvider.class);

    @Override
    public IQueryApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        if (VERSION_2023_05_26.equals(apiVersion)) {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_05_26);
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
