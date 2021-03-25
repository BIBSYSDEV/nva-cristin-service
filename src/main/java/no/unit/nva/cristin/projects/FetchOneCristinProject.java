package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.ID;
import static no.unit.nva.cristin.projects.RequestUtils.getValidLanguage;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchOneCristinProject extends ApiGatewayHandler<Void, NvaProject> {

    public static final String INVALID_PATH_PARAMETER_FOR_ID_EXCEPTION_MESSAGE =
        "Invalid path parameter for id, needs to be a number";

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

        String language = getValidLanguage(requestInfo);
        String id = getValidId(requestInfo);

        return getTransformedProjectFromCristin(id, language);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, NvaProject output) {
        return HttpURLConnection.HTTP_OK;
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Integer.parseInt(requestInfo.getPathParameter(ID)))
            .orElseThrow(failure -> new BadRequestException(INVALID_PATH_PARAMETER_FOR_ID_EXCEPTION_MESSAGE));

        return requestInfo.getPathParameter(ID);
    }

    private NvaProject getTransformedProjectFromCristin(String id, String language) {
        return cristinApiClient.queryOneCristinProjectUsingIdIntoNvaProject(id, language);
    }
}
