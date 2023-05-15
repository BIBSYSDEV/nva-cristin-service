package no.unit.nva.cristin.organization;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.organization.QueryCristinOrganizationHandler.getValidDepth;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.regex.Pattern;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchCristinOrganizationHandler extends CristinQueryHandler<Void, Organization> {

    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    private final transient IClientProvider
                                <RequestInfo, IFetchApiClient<Map<String, String>, Organization>> clientProvider;

    @JacocoGenerated
    public FetchCristinOrganizationHandler() {
        this(new DefaultOrgFetchProvider(), new Environment());
    }

    public FetchCristinOrganizationHandler(IClientProvider
                                               <RequestInfo, IFetchApiClient<Map<String, String>, Organization>>
                                               clientProvider,
                                           Environment environment) {
        super(Void.class, environment);
        this.clientProvider = clientProvider;
    }

    @SuppressWarnings("PMD.AvoidThrowingNewInstanceOfSameException")
    @Override
    protected Organization processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateThatSuppliedParamsIsSupported(requestInfo);
        final String identifier = getValidId(requestInfo);
        final String depth = getValidDepth(requestInfo);

        var params = Map.of(DEPTH, depth, IDENTIFIER, identifier);

        return clientProvider.getClient(requestInfo).executeFetch(params);
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
