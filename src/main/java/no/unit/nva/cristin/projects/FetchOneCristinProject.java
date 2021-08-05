package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.ID;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Set;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchOneCristinProject extends CristinHandler<Void, NvaProject> {

    private static final Set<String> VALID_QUERY_PARAMS = Set.of(LANGUAGE);

    private final transient CristinApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchOneCristinProject() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchOneCristinProject(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    public FetchOneCristinProject(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected NvaProject processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateThatSuppliedQueryParamsIsSupported(requestInfo);

        String language = getValidLanguage(requestInfo);
        String id = getValidId(requestInfo);

        addRequestedContentTypeToResponseIfSupported(requestInfo);

        return getTransformedProjectFromCristin(id, language);
    }

    private void validateThatSuppliedQueryParamsIsSupported(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, NvaProject output) {
        return HttpURLConnection.HTTP_OK;
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Integer.parseInt(requestInfo.getPathParameter(ID)))
            .orElseThrow(failure -> new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID));

        return requestInfo.getPathParameter(ID);
    }

    private NvaProject getTransformedProjectFromCristin(String id, String language) throws ApiGatewayException {
        return cristinApiClient.queryOneCristinProjectUsingIdIntoNvaProject(id, language);
    }
}
