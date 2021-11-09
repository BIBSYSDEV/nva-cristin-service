package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.organization.utils.Language;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.projects.Constants.IDENTIFIER;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_LANGUAGE_INVALID;
import static nva.commons.core.attempt.Try.attempt;

public class FetchCristinOrganizationHandler extends ApiGatewayHandler<Void, NvaOrganization> {

    public static final String IDENTIFIER_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    private static final Set<String> VALID_QUERY_PARAMS = Set.of(LANGUAGE);
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb", "nn");
    protected static final String DEFAULT_LANGUAGE_CODE = "nb";

    private final transient CristinApiClient cristinApiClient;

    public FetchCristinOrganizationHandler() {
        this(new Environment());
    }

    public FetchCristinOrganizationHandler(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    public FetchCristinOrganizationHandler(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }


    @Override
    protected NvaOrganization processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateThatSuppliedQueryParamsIsSupported(requestInfo);

        String language = getValidLanguage(requestInfo);
        String id = getValidId(requestInfo);

        try {
            return getTransformedOrganizationFromCristin(id);
        } catch (InterruptedException e) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, NvaOrganization output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateThatSuppliedQueryParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Pattern.compile(IDENTIFIER_PATTERN).matcher(requestInfo.getPathParameter(IDENTIFIER)).find())
                .orElseThrow(failure -> new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID));

        return requestInfo.getPathParameter(IDENTIFIER);
    }

    protected static String getValidLanguage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo, LANGUAGE)
                        .orElse(DEFAULT_LANGUAGE_CODE))
                .filter(VALID_LANGUAGE_CODES::contains)
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    private NvaOrganization getTransformedOrganizationFromCristin(String identifier) throws ApiGatewayException, InterruptedException {
        return Optional.of(cristinApiClient.getSingleUnit(URI.create(identifier), Language.DEFAULT_LANGUAGE))
                          .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo, String queryParameter) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter)).toOptional();
    }
}
