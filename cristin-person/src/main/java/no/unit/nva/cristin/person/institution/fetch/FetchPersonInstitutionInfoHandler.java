package no.unit.nva.cristin.person.institution.fetch;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.List;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.model.nva.PersonInstitutionInfo;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchPersonInstitutionInfoHandler extends ApiGatewayHandler<Void, PersonInstitutionInfo> {

    public static final String PERSON_ID = "id";
    public static final String ORG_ID = "orgId";
    public static final String PUNCTUATION = ".";
    public static final String INVALID_ORGANIZATION_ID = "Invalid path parameter for organization id";
    public static final String INVALID_PERSON_ID = "Invalid path parameter for person id";

    private final transient FetchPersonInstitutionInfoClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchPersonInstitutionInfoHandler() {
        this(new FetchPersonInstitutionInfoClient(HttpClient.newHttpClient()), new Environment());
    }

    public FetchPersonInstitutionInfoHandler(FetchPersonInstitutionInfoClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
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

    private void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_LOOKUP);
        }
    }

    private String getValidPersonId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(PERSON_ID)).orElse(fail -> EMPTY_STRING);
        if (isValidIdentifier(identifier)) {
            return identifier;
        }
        throw new BadRequestException(INVALID_PERSON_ID);
    }

    private String getValidOrgId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(ORG_ID)).orElse(fail -> EMPTY_STRING);
        String cristinInstitutionId = removeUnitIfPresent(identifier);
        if (isValidIdentifier(cristinInstitutionId)) {
            return cristinInstitutionId;
        }
        throw new BadRequestException(INVALID_ORGANIZATION_ID);
    }

    private String removeUnitIfPresent(String identifier) {
        if (identifier.contains(PUNCTUATION)) {
            return identifier.substring(0, identifier.indexOf(PUNCTUATION));
        }
        return identifier;
    }

    private boolean isValidIdentifier(String identifier) {
        return Utils.isPositiveInteger(identifier);
    }
}
