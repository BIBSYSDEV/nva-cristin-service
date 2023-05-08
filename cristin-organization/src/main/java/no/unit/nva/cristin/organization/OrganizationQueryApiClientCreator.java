package no.unit.nva.cristin.organization;

import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;

public class OrganizationQueryApiClientCreator {

    public static final String VERSION = "version";
    public static final String VERSION_TWO = "2";

    /**
     * Creates an api client based on version information from request query param.
     *
     * @param requestInfo the object containing request query parameters
     */
    public IQueryApiClient<Organization> createFromVersionParam(RequestInfo requestInfo) {
        return createApiClientFromVersion(requestInfo);
    }

    private IQueryApiClient<Organization> createApiClientFromVersion(RequestInfo requestInfo) {
        return clientRequestsVersionTwo(requestInfo)
                   ? new CristinOrganizationApiClientV2() : new CristinOrganizationApiClient();
    }

    private boolean clientRequestsVersionTwo(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(VERSION).map(VERSION_TWO::equals).isPresent();
    }
}
