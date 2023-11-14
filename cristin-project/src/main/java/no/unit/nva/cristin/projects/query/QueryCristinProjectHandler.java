package no.unit.nva.cristin.projects.query;

import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_CURRENT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_ITEMS_PER_PAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_PROJECT;
import static no.unit.nva.utils.VersioningUtils.ACCEPT_HEADER_KEY_NAME;
import static no.unit.nva.utils.VersioningUtils.extractVersionFromRequestInfo;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.client.ClientProvider;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.ParameterKeyProject;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

/**
 * Handler for requests to Lambda function.
 */
public class QueryCristinProjectHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final ParameterKeyProject[] REQUIRED_QUERY_PARAMETER =
        {PATH_PROJECT, PAGE_CURRENT, PAGE_ITEMS_PER_PAGE };

    private final transient ClientProvider<CristinQueryApiClient<QueryProject, NvaProject>> clientProvider;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public QueryCristinProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinProjectHandler(Environment environment) {
        this(new DefaultProjectQueryClientProvider(), environment);
    }

    protected QueryCristinProjectHandler(
        ClientProvider<CristinQueryApiClient<QueryProject, NvaProject>> clientProvider,
        Environment environment
    ) {
        super(Void.class, environment);
        this.clientProvider = clientProvider;
    }

    @Override
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        final var cristinQuery = (QueryProject)
            QueryProject.builder()
                .fromRequestInfo(requestInfo)
                .withRequiredParameters(REQUIRED_QUERY_PARAMETER)
                .build();

        var apiVersion = getApiVersion(requestInfo);
        return clientProvider.getClient(apiVersion).executeQuery(cristinQuery);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

    private String getApiVersion(RequestInfo requestInfo) {
        return extractVersionFromRequestInfo(requestInfo, ACCEPT_HEADER_KEY_NAME);
    }

}
