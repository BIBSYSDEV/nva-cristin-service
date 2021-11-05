package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.projects.CristinApiClient;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class CristinApiClientStub extends CristinApiClient {

    @Override
    public NvaOrganization getOrganizationByIdentifier(String identifier)  throws ApiGatewayException {
        return null;
    }
}
