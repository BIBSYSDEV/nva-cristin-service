package no.unit.nva.cristin.person.query;

import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.Map;
import no.unit.nva.client.ClientProvider;
import no.unit.nva.cristin.common.client.CristinAuthorizedQueryClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.query.v2.QueryPersonWithFacetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPersonQueryClientProvider
    implements ClientProvider<CristinAuthorizedQueryClient<Map<String, String>, Person>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPersonQueryClientProvider.class);

    public static final String VERSION_2023_11_03 = "2023-11-03";

    @Override
    public CristinAuthorizedQueryClient<Map<String, String>, Person> getClient(String apiVersion) {
        return switch (nonNull(apiVersion) ? apiVersion : EMPTY_STRING) {
            case VERSION_2023_11_03 -> {
                logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_2023_11_03);
                yield getVersion20231103();
            }
            case VERSION_ONE -> {
                logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
                yield getVersionOne();
            }
            default -> {
                logger.info(CLIENT_DID_NOT_SPECIFY_VERSION_RETURNING_DEFAULT, VERSION_ONE);
                yield getVersionOne();
            }
        };
    }

    public CristinAuthorizedQueryClient<Map<String, String>, Person> getVersionOne() {
        return new CristinPersonApiClient();
    }

    public CristinAuthorizedQueryClient<Map<String, String>, Person> getVersion20231103() {
        return new QueryPersonWithFacetsClient();
    }

}
