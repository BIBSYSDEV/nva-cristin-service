package no.unit.nva.cristin.person.employment.query;

import java.net.http.HttpClient;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;

public class QueryPersonEmploymentClient extends ApiClient {

    public QueryPersonEmploymentClient(HttpClient client) {
        super(client);
    }

    public CristinPersonEmployment queryUpstreamUsingIdentifier(String identifier) {
        CristinPersonEmployment employment = new CristinPersonEmployment();
        employment.setIdentifier(identifier);
        return employment;
    }
}
