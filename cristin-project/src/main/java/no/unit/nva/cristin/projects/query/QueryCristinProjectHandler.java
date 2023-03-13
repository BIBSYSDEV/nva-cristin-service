package no.unit.nva.cristin.projects.query;

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.LANGUAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_CURRENT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_ITEMS_PER_PAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_PROJECT;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Set;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.projects.common.ParameterKeyProject;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

/**
 * Handler for requests to Lambda function.
 */
public class QueryCristinProjectHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Set<String> VALID_QUERY_PARAMETERS = ParameterKeyProject.VALID_QUERY_PARAMETER_NVA_KEYS;
    public static final ParameterKeyProject[] REQUIRED_QUERY_PARAMETER =
        {PATH_PROJECT, PAGE_CURRENT, PAGE_ITEMS_PER_PAGE, LANGUAGE};

    private final transient QueryCristinProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public QueryCristinProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinProjectHandler(Environment environment) {
        this(new QueryCristinProjectApiClient(), environment);
    }

    protected QueryCristinProjectHandler(QueryCristinProjectApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        final var cristinQuery = (QueryProject)
            QueryProject.builder()
                .fromRequestInfo(requestInfo)
                .withRequiredParameters(REQUIRED_QUERY_PARAMETER)
                .asNvaQuery()
                .build();
        return cristinApiClient.queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(cristinQuery);
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

}
