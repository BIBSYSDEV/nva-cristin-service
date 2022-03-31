package no.unit.nva.cristin.person.employment.create;

import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.exception.UnauthorizedException;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class CreatePersonEmploymentHandler extends ApiGatewayHandler<Employment, Employment> {

    private final transient CreatePersonEmploymentClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public CreatePersonEmploymentHandler() {
        this(new CreatePersonEmploymentClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    @JacocoGenerated
    public CreatePersonEmploymentHandler(CreatePersonEmploymentClient apiClient, Environment environment) {
        super(Employment.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Employment processInput(Employment input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateHasAccessRights(requestInfo);

        String identifier = getValidPersonId(requestInfo);

        return apiClient.createEmploymentInCristin(identifier, input);
    }

    @Override
    protected Integer getSuccessStatusCode(Employment input, Employment output) {
        return HttpURLConnection.HTTP_CREATED;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException, UnauthorizedException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }
}
