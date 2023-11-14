package no.unit.nva.cristin.projects.query.version.facet;

import no.unit.nva.client.ClientVersion;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class QueryProjectWithFacetsClient extends CristinProjectApiClient
    implements ClientVersion, CristinQueryApiClient<QueryProject, NvaProject> {

    public static final String VERSION_WITH_AGGREGATIONS = "2023-11-03-aggregations";

    @Override
    public String getClientVersion() {
        return VERSION_WITH_AGGREGATIONS;
    }

    @Override
    public SearchResponse<NvaProject> executeQuery(QueryProject params) throws ApiGatewayException {
        return null;
    }

}
