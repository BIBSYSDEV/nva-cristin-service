package no.unit.nva.cristin.person.query;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.person.query.version.facet.QueryPersonWithFacetsClient.VERSION_WITH_AGGREGATIONS;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.Map;
import no.unit.nva.client.ClientProvider;
import no.unit.nva.cristin.common.client.CristinAuthorizedQueryClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.query.version.facet.QueryPersonWithFacetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPersonQueryClientProvider
    implements ClientProvider<CristinAuthorizedQueryClient<Map<String, String>, Person>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPersonQueryClientProvider.class);
    public static final String VERSION_AGGREGATIONS_NAME_ALIAS = "aggregations";
    public static final String VERSION_AGGREGATIONS_DATE_ALIAS = "2023-11-03";

    @Override
    public CristinAuthorizedQueryClient<Map<String, String>, Person> getClient(String apiVersion) {
        return switch (nonNull(apiVersion) ? apiVersion : EMPTY_STRING) {
            case VERSION_WITH_AGGREGATIONS, VERSION_AGGREGATIONS_NAME_ALIAS, VERSION_AGGREGATIONS_DATE_ALIAS -> {
                logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_WITH_AGGREGATIONS);
                yield getVersionWithFacets();
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

    public CristinAuthorizedQueryClient<Map<String, String>, Person> getVersionWithFacets() {
        return new QueryPersonWithFacetsClient();
    }

}
