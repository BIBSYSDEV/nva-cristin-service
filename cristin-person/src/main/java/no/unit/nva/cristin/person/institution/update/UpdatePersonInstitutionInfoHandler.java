package no.unit.nva.cristin.person.institution.update;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.cristin.person.model.nva.PersonInstInfoPatch;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class UpdatePersonInstitutionInfoHandler extends ApiGatewayHandler<PersonInstInfoPatch, String> {

    public static final String EMPTY_JSON = "{}";

    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdatePersonInstitutionInfoHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public UpdatePersonInstitutionInfoHandler(Environment environment) {
        super(PersonInstInfoPatch.class, environment);
    }

    @Override
    protected String processInput(PersonInstInfoPatch input, RequestInfo requestInfo, Context context)
        throws ForbiddenException {

        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        return EMPTY_JSON;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected Integer getSuccessStatusCode(PersonInstInfoPatch input, String output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }
}
