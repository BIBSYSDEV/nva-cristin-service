package no.unit.nva.cristin.organization;

import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOrgQueryClientProvider implements IClientProvider<RequestInfo, IQueryApiClient<Organization>> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOrgQueryClientProvider.class);

    public static final String VERSION = "version";
    public static final String VERSION_ONE = "1";
    public static final String VERSION_TWO = "2";
    public static final String CLIENT_WANTS_VERSION_OF_THE_API_CLIENT = "Client wants version {} of the api client";

    @Override
    public IQueryApiClient<Organization> getClient(RequestInfo requestInfo) {
        if (clientRequestsVersionTwo(requestInfo)) {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_TWO);
            return getVersionTwo();
        } else {
            logger.info(CLIENT_WANTS_VERSION_OF_THE_API_CLIENT, VERSION_ONE);
            return getVersionOne();
        }
    }

    private boolean clientRequestsVersionTwo(RequestInfo requestInfo) {
        return requestInfo
                   .getQueryParameterOpt(VERSION)
                   .filter(VERSION_TWO::equals)
                   .isPresent();
    }

    public IQueryApiClient<Organization> getVersionOne() {
        return new CristinOrganizationApiClient();
    }

    public IQueryApiClient<Organization> getVersionTwo() {
        return new CristinOrgApiClientVersion2();
    }
}
