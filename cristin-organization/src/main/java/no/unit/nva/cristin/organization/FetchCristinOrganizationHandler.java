package no.unit.nva.cristin.organization;


import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.model.Organization;
import no.unit.nva.utils.Language;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import static no.unit.nva.cristin.projects.Constants.IDENTIFIER;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_LANGUAGE_INVALID;
import static nva.commons.core.attempt.Try.attempt;

public class FetchCristinOrganizationHandler extends ApiGatewayHandler<Void, Organization> {

    public static final String IDENTIFIER_PATTERN = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$";
    public static final Pattern PATTERN = Pattern.compile(IDENTIFIER_PATTERN);
    protected static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final Set<String> VALID_QUERY_PARAMS = Set.of(LANGUAGE);
    private static final Set<String> VALID_LANGUAGE_CODES = Set.of("en", "nb", "nn");
    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public FetchCristinOrganizationHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchCristinOrganizationHandler(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    public FetchCristinOrganizationHandler(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    protected static String getValidLanguage(RequestInfo requestInfo) throws BadRequestException {
        return Optional.of(getQueryParam(requestInfo)
                        .orElse(DEFAULT_LANGUAGE_CODE))
                .filter(VALID_LANGUAGE_CODES::contains)
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    protected static Optional<String> getQueryParam(RequestInfo requestInfo) {
        return attempt(() -> requestInfo.getQueryParameter(LANGUAGE)).toOptional();
    }

    @Override
    protected Organization processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        Organization result;
        validateThatSuppliedQueryParamsIsSupported(requestInfo);
        try {
            result = getTransformedOrganizationFromCristin(getValidId(requestInfo),
                    Language.getLanguage(getValidLanguage(requestInfo)));
        } catch (InterruptedException e) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
        return result;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, Organization output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateThatSuppliedQueryParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
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

    private Organization getTransformedOrganizationFromCristin(String identifier, Language language)
            throws ApiGatewayException, InterruptedException {
        return Optional.of(cristinApiClient.getSingleUnit(createOrganizationUri(identifier), language))
                .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_LANGUAGE_INVALID));
    }

    private URI createOrganizationUri(String identifier) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(ORGANIZATION_PATH)
                .addChild(identifier).getUri();
    }
}
