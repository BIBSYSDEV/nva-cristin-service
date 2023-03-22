package no.unit.nva.cristin.projects.query.organization;

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.QueryParameterKey.BIOBANK;
import static no.unit.nva.cristin.model.QueryParameterKey.FUNDING;
import static no.unit.nva.cristin.model.QueryParameterKey.FUNDING_SOURCE;
import static no.unit.nva.cristin.model.QueryParameterKey.INSTITUTION;
import static no.unit.nva.cristin.model.QueryParameterKey.LEVELS;
import static no.unit.nva.cristin.model.QueryParameterKey.PAGE_CURRENT;
import static no.unit.nva.cristin.model.QueryParameterKey.PAGE_ITEMS_PER_PAGE;
import static no.unit.nva.cristin.model.QueryParameterKey.PAGE_SORT;
import static no.unit.nva.cristin.model.QueryParameterKey.PATH_ORGANISATION;
import static no.unit.nva.cristin.model.QueryParameterKey.PATH_PROJECT;
import static no.unit.nva.cristin.model.QueryParameterKey.APPROVAL_REFERENCE_ID;
import static no.unit.nva.cristin.model.QueryParameterKey.APPROVED_BY;
import static no.unit.nva.cristin.model.QueryParameterKey.KEYWORD;
import static no.unit.nva.cristin.model.QueryParameterKey.PROJECT_MANAGER;
import static no.unit.nva.cristin.model.QueryParameterKey.MODIFIED_SINCE;
import static no.unit.nva.cristin.model.QueryParameterKey.PARTICIPANT;
import static no.unit.nva.cristin.model.QueryParameterKey.PROJECT_UNIT;
import static no.unit.nva.cristin.model.QueryParameterKey.USER;
import static no.unit.nva.cristin.model.QueryParameterKey.VALID_QUERY_PARAMETER_KEYS;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Set;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.QueryParameterKey;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinQuery;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings({"Unused", "UnusedPrivateField"})
public class QueryCristinOrganizationProjectHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Set<String> VALID_QUERY_PARAM_NO_VALIDATION =
            Set.of(INSTITUTION.getNvaKey(), PROJECT_MANAGER.getNvaKey(),
                   PARTICIPANT.getNvaKey(), KEYWORD.getNvaKey(),
                   FUNDING_SOURCE.getNvaKey(), FUNDING.getNvaKey(),
                   APPROVAL_REFERENCE_ID.getNvaKey(), APPROVED_BY.getNvaKey(),
                   PAGE_SORT.getNvaKey(), PROJECT_UNIT.getNvaKey(), USER.getNvaKey(),
                   LEVELS.getNvaKey(), BIOBANK.getNvaKey(), MODIFIED_SINCE.getNvaKey());

    public static final QueryParameterKey[] REQUIRED_QUERY_PARAMETER =
        {PATH_ORGANISATION, PATH_PROJECT, PAGE_CURRENT, PAGE_ITEMS_PER_PAGE};

    private final transient QueryCristinOrganizationProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public QueryCristinOrganizationProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinOrganizationProjectHandler(Environment environment) {
        this(new QueryCristinOrganizationProjectApiClient(), environment);
    }

    protected QueryCristinOrganizationProjectHandler(QueryCristinOrganizationProjectApiClient cristinApiClient,
                                                     Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param input       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {
        var cristinQuery =
            CristinQuery.builder()
                .fromRequestInfo(requestInfo)
                .withRequiredParameters(REQUIRED_QUERY_PARAMETER)
                .asNvaQuery()
                .validate()
                .build();

        return cristinApiClient.listOrganizationProjects(cristinQuery);
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<NvaProject> output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAM_NO_VALIDATION.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETER_KEYS));
        }
    }
}
