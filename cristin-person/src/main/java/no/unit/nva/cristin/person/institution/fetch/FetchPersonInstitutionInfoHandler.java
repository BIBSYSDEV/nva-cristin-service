package no.unit.nva.cristin.person.institution.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.institution.common.PersonInstitutionInfoHandler;
import no.unit.nva.cristin.person.model.nva.PersonInstitutionInfo;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cristin.common.Utils.getValidOrgId;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

public class FetchPersonInstitutionInfoHandler extends PersonInstitutionInfoHandler<Void, PersonInstitutionInfo> {

    private static final Logger logger = LoggerFactory.getLogger(FetchPersonInstitutionInfoHandler.class);

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

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

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
