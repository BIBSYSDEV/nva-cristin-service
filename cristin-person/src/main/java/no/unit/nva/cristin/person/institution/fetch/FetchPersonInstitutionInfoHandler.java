package no.unit.nva.cristin.person.institution.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.institution.common.PersonInstitutionInfoHandler;
import no.unit.nva.cristin.person.model.nva.PersonInstitutionInfo;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPersonInstitutionInfoHandler extends PersonInstitutionInfoHandler<Void, PersonInstitutionInfo> {

    private final transient FetchPersonInstitutionInfoClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchPersonInstitutionInfoHandler() {
        this(new FetchPersonInstitutionInfoClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public FetchPersonInstitutionInfoHandler(FetchPersonInstitutionInfoClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected PersonInstitutionInfo processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        validateQueryParameters(requestInfo);
        String personId = getValidPersonId(requestInfo);
        String orgId = getValidOrgId(requestInfo);

        return apiClient.generateGetResponse(personId, orgId);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, PersonInstitutionInfo output) {
        return HttpURLConnection.HTTP_OK;
    }

}
