package no.unit.nva.cristin.projects.query;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.projects.query.version.facet.QueryProjectWithFacetsClient.VERSION_WITH_AGGREGATIONS;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import no.unit.nva.client.ClientProvider;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.query.version.facet.QueryProjectWithFacetsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProjectQueryClientProvider
    implements ClientProvider<CristinQueryApiClient<QueryProject, NvaProject>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProjectQueryClientProvider.class);

    public static final String VERSION_AGGREGATIONS_NAME_ALIAS = "aggregations";
    public static final String VERSION_AGGREGATIONS_DATE_ALIAS = "2023-11-03";

    @Override
    public CristinQueryApiClient<QueryProject, NvaProject> getClient(String apiVersion) {
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

    protected CristinQueryApiClient<QueryProject, NvaProject> getVersionOne() {
        return new QueryCristinProjectApiClient();
    }

    protected CristinQueryApiClient<QueryProject, NvaProject> getVersionWithFacets() {
        return new QueryProjectWithFacetsClient();
    }
}
