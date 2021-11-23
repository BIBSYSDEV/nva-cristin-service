package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_BASE;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;

public class FetchCristinOrganizationHandler extends ApiGatewayHandler<Void, Organization> {

    public static final String IDENTIFIER_PATTERN = "^(?:[0-9]+\\.){3}[0-9]{1,3}$";
    public static final Pattern PATTERN = Pattern.compile(IDENTIFIER_PATTERN);
    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public FetchCristinOrganizationHandler() {
        this(new CristinApiClient());
    }

    public FetchCristinOrganizationHandler(CristinApiClient cristinApiClient) {
        super(Void.class);
        this.cristinApiClient = cristinApiClient;
    }

    @SuppressWarnings("PMD.AvoidThrowingNewInstanceOfSameException")
    @Override
    protected Organization processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        Organization result;
        validateThatSuppliedParamsIsSupported(requestInfo);
        final String identifier = getValidId(requestInfo);
        try {
            result = getTransformedOrganizationFromCristin(identifier);
        } catch (NotFoundException e) {
            URI uri = new UriWrapper(HTTPS, DOMAIN_NAME)
                    .addChild(BASE_PATH)
                    .addChild(ORGANIZATION_PATH)
                    .addChild(identifier)
                    .getUri();
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, uri));
        } catch (InterruptedException e) {
            throw new BadRequestException(ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE);
        }
        result.setContext(ORGANIZATION_CONTEXT);
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Organization output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateThatSuppliedParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        final String identifier = requestInfo.getPathParameter(IDENTIFIER);
        if (matchesIdentifierPattern(identifier)) {
            return identifier;
        } else {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
        }
    }

    private boolean matchesIdentifierPattern(String identifier) {
        return PATTERN.matcher(identifier).matches();
    }

    private Organization getTransformedOrganizationFromCristin(String identifier)
            throws ApiGatewayException, InterruptedException {
        return Optional.of(cristinApiClient.getOrganization(new UriWrapper(HTTPS,
                        CRISTIN_API_BASE)
                        .addChild(UNITS_PATH)
                        .addChild(identifier)
                        .getUri()))
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE));
    }

}
