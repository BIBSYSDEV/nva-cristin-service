package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

public class FetchCristinOrganizationHandler extends CristinQueryHandler<Void, Organization> {

    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public FetchCristinOrganizationHandler() {
        this(new CristinApiClient(), new Environment());
    }

    public FetchCristinOrganizationHandler(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @SuppressWarnings("PMD.AvoidThrowingNewInstanceOfSameException")
    @Override
    protected Organization processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        validateThatSuppliedParamsIsSupported(requestInfo);
        final String identifier = getValidId(requestInfo);
        try {
            Organization organization = cristinApiClient.getOrganization(getCristinUri(identifier, UNITS_PATH));
            organization.setContext(ORGANIZATION_CONTEXT);
            return organization;
        } catch (NotFoundException e) {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE,
                    getNvaApiId(identifier, ORGANIZATION_PATH)));
        } catch (InterruptedException e) {
            throw new BadRequestException(ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE);
        }
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
        if (PATTERN.matcher(identifier).matches()) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
    }

}
