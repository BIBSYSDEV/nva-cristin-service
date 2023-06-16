package no.unit.nva.cristin.organization.fetch;

import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.Map;
import no.unit.nva.cristin.common.client.ClientProvider;
import no.unit.nva.cristin.common.client.FetchApiClient;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.cristin.organization.common.client.v20230526.FetchCristinOrgClient20230526;
import no.unit.nva.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgFetchClientProvider
    implements ClientProvider<FetchApiClient<Map<String, String>, Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgFetchClientProvider.class);

    @Override
    public FetchApiClient<Map<String, String>, Organization> getClient(String apiVersion) {
        switch (nonNull(apiVersion) ? apiVersion : EMPTY_STRING) {
            case VERSION_2023_05_26:
                logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_05_26);
                return getVersion20230526();
            case VERSION_ONE:
                logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
                return getVersionOne();
            default:
                logger.info(CLIENT_DID_NOT_SPECIFY_VERSION_RETURNING_DEFAULT, VERSION_ONE);
                return getVersionOne();
        }
    }

    protected FetchApiClient<Map<String, String>, Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }

    protected FetchApiClient<Map<String, String>, Organization> getVersion20230526() {
        return new FetchCristinOrgClient20230526();
    }
}
