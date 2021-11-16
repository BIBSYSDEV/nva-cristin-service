package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_HOST;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_LANGUAGE_INVALID;

public class FetchCristinOrganizationHandler extends ApiGatewayHandler<Void, Organization> {

    public static final String IDENTIFIER_PATTERN = "^(?:[0-9]{1,4}\\.){3}[0-9]{1,3}$";
    public static final Pattern PATTERN = Pattern.compile(IDENTIFIER_PATTERN);
    private static final String VERSION2 = "v2";
    private static final String UNITS_PATH = "units";
    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public FetchCristinOrganizationHandler() {
        this(new CristinApiClient());
    }

    public FetchCristinOrganizationHandler(CristinApiClient cristinApiClient) {
        super(Void.class);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected Organization processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        Organization result;
        validateThatSuppliedParamsIsSupported(requestInfo);
        try {
            result = getTransformedOrganizationFromCristin(getValidId(requestInfo));
        } catch (InterruptedException e) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Organization output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateThatSuppliedParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        final String identifier =  requestInfo.getPathParameter(IDENTIFIER);
        if (matchesIdentifierPattern(identifier)) {
            return identifier;
        } else {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID);
        }
    }

    private boolean matchesIdentifierPattern(String identifier) {
        return PATTERN.matcher(identifier).matches();
    }

    private Organization getTransformedOrganizationFromCristin(String identifier)
            throws ApiGatewayException, InterruptedException {
        return Optional.of(cristinApiClient.getSingleUnit(createCristinOrganizationUri(identifier)))
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    public static URI createCristinOrganizationUri(String identifier) {
        return new UriWrapper(HTTPS, CRISTIN_API_HOST).addChild(VERSION2).addChild(UNITS_PATH)
                .addChild(identifier).addQueryParameter("lang","en,nb,nn").getUri();
    }
}
