package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.attempt.Try;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.model.Constants.QueryType;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static no.unit.nva.cristin.projects.CristinQuery.CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID;
import static no.unit.nva.utils.UriUtils.PROJECT;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.queryParameters;
import static nva.commons.core.attempt.Try.attempt;

public class CristinApiClient extends ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    /**
     * Create a generic cristin API client with default HTTP client.
     */
    public CristinApiClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }


    public CristinApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Creates a NvaProject object containing a single transformed Cristin Project. Is used for serialization to the
     * client.
     *
     * @param id       The Cristin id of the project to query
     * @param language Language used for some properties in Cristin API response
     * @return a NvaProject filled with one transformed Cristin Project
     * @throws ApiGatewayException when there is a problem that can be returned to client
     */
    public NvaProject queryOneCristinProjectUsingIdIntoNvaProject(String id, String language)
            throws ApiGatewayException {

        return Optional.of(getProject(id, language))
                .filter(CristinProject::hasValidContent)
                .map(NvaProjectBuilder::new)
                .map(builder -> builder.withContext(PROJECT_LOOKUP_CONTEXT_URL))
                .map(NvaProjectBuilder::build)
                .orElseThrow(() -> projectHasNotValidContent(id));
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param requestQueryParameters Request parameters from client containing title and language
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
            Map<String, String> requestQueryParameters) throws ApiGatewayException {

        long startRequestTime = System.currentTimeMillis();
        QueryType queryType = getQueryTypeBasedOnParameters(requestQueryParameters);
        HttpResponse<String> response = queryProjects(requestQueryParameters, queryType);
        List<CristinProject> cristinProjects =
                getEnrichedProjectsUsingQueryResponse(response, requestQueryParameters.get(LANGUAGE));
        if (cristinProjects.isEmpty() && queryType == QUERY_USING_GRANT_ID) {
            response = queryProjects(requestQueryParameters, QUERY_USING_TITLE);
            cristinProjects = getEnrichedProjectsUsingQueryResponse(response, requestQueryParameters.get(LANGUAGE));
        }
        List<NvaProject> nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        long endRequestTime = System.currentTimeMillis();

        URI id = createIdUriFromParams(rewrapOrganizationUri(requestQueryParameters), PROJECT);

        return new SearchResponse<NvaProject>(id)
                .withContext(PROJECT_SEARCH_CONTEXT_URL)
                .usingHeadersAndQueryParams(response.headers(), requestQueryParameters)
                .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                .withHits(nvaProjects);
    }

    /**
     * Searches for an Organizations projects for a given parent_unit.
     * @param requestQueryParameters parametes for search containg parent_unit_id
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> listOrganizationProjects(Map<String, String> requestQueryParameters)
            throws ApiGatewayException {

        long startRequestTime = System.currentTimeMillis();
        URI cristinUri = new CristinQuery()
                .withParentUnitId(requestQueryParameters.get(CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID))
                .withFromPage(requestQueryParameters.get(PAGE))
                .withLanguage(requestQueryParameters.get(LANGUAGE))
                .withItemsPerPage(requestQueryParameters.get(NUMBER_OF_RESULTS))
                .toURI();
        HttpResponse<String> response = listProjects(cristinUri);
        List<CristinProject> cristinProjects =
                getEnrichedProjectsUsingQueryResponse(response, requestQueryParameters.get(LANGUAGE));
        List<NvaProject> nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        long endRequestTime = System.currentTimeMillis();

        URI id = getServiceUri(new HashMap(requestQueryParameters));

        return new SearchResponse<NvaProject>(id)
                .withContext(PROJECT_SEARCH_CONTEXT_URL)
                .usingHeadersAndQueryParams(response.headers(), requestQueryParameters)
                .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                .withHits(nvaProjects);
    }

    protected HttpResponse<String> queryProjects(Map<String, String> parameters, QueryType queryType)
            throws ApiGatewayException {

        URI uri = attempt(() -> generateQueryProjectsUrl(parameters, queryType))
                .toOptional(failure -> logError(ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED, queryParameters(parameters),
                        failure.getException()))
                .orElseThrow();
        HttpResponse<String> response = fetchQueryResults(uri);
        URI id = createIdUriFromParams(parameters, PROJECT);
        checkHttpStatusCode(id, response.statusCode());
        return response;
    }

    protected HttpResponse<String> listProjects(URI uri) throws ApiGatewayException {
        HttpResponse<String> response = fetchQueryResults(uri);
        checkHttpStatusCode(uri, response.statusCode());
        return response;
    }

    protected CristinProject getProject(String id, String language) throws ApiGatewayException {
        URI uri = attempt(() -> generateGetProjectUri(id, language))
            .toOptional(failure -> logError(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID, id, failure.getException()))
            .orElseThrow();

        HttpResponse<String> response = fetchGetResult(uri);
        checkHttpStatusCode(UriUtils.getNvaApiId(id, PROJECT), response.statusCode());

        return getDeserializedResponse(response, CristinProject.class);
    }

    protected List<CristinProject> getEnrichedProjectsUsingQueryResponse(HttpResponse<String> response, String language)
            throws ApiGatewayException {

        List<CristinProject> projectsFromQuery = asList(getDeserializedResponse(response, CristinProject[].class));
        List<URI> cristinUris = extractCristinUrisFromProjects(language, projectsFromQuery);
        List<HttpResponse<String>> individualResponses = fetchQueryResultsOneByOne(cristinUris);

        List<CristinProject> enrichedCristinProjects = mapValidResponsesToCristinProjects(individualResponses);

        return allProjectsWereEnriched(projectsFromQuery, enrichedCristinProjects)
                ? enrichedCristinProjects
                : combineResultsWithQueryInCaseEnrichmentFails(projectsFromQuery, enrichedCristinProjects);
    }

    protected List<CristinProject> combineResultsWithQueryInCaseEnrichmentFails(List<CristinProject> projectsFromQuery,
                                                                                List<CristinProject> enrichedProjects) {
        Set<String> enrichedProjectIds = enrichedProjects.stream()
                .map(CristinProject::getCristinProjectId)
                .collect(Collectors.toSet());

        List<CristinProject> missingProjects = projectsFromQuery.stream()
                .filter(queryProject -> !enrichedProjectIds.contains(queryProject.getCristinProjectId()))
                .collect(Collectors.toList());

        ArrayList<CristinProject> result = new ArrayList<>();
        result.addAll(enrichedProjects);
        result.addAll(missingProjects);
        return result;
    }

    private boolean allProjectsWereEnriched(List<CristinProject> projectsFromQuery,
                                            List<CristinProject> enrichedCristinProjects) {
        return projectsFromQuery.size() == enrichedCristinProjects.size();
    }

    private List<URI> extractCristinUrisFromProjects(String language, List<CristinProject> projectsFromQuery) {
        return projectsFromQuery.stream()
                .map(attempt(project -> generateGetProjectUri(project.getCristinProjectId(), language)))
                .map(Try::orElseThrow)
                .collect(Collectors.toList());
    }

    private QueryType getQueryTypeBasedOnParameters(Map<String, String> requestQueryParams) {
        return Utils.isPositiveInteger(requestQueryParams.get(QUERY)) ? QUERY_USING_GRANT_ID : QUERY_USING_TITLE;
    }

    protected URI generateQueryProjectsUrl(Map<String, String> parameters, QueryType queryType)
            throws URISyntaxException {

        CristinQuery query = new CristinQuery()
                .withLanguage(parameters.get(LANGUAGE))
                .withFromPage(parameters.get(PAGE))
                .withItemsPerPage(parameters.get(NUMBER_OF_RESULTS));
        if (parameters.containsKey(ORGANIZATION)) {
            query = query.withParentUnitId(getUnitIdFromOrganization(parameters.get(ORGANIZATION)));
        }

        if (parameters.containsKey(STATUS)) {
            query = query.withStatus(getEncodedStatusParameter(parameters));
        }

        return queryType == QUERY_USING_GRANT_ID ? query.withGrantId(parameters.get(QUERY)).toURI() :
                query.withTitle(parameters.get(QUERY)).toURI();
    }

    private String getEncodedStatusParameter(Map<String, String> parameters) {
        return URLEncoder.encode(ProjectStatus.getNvaStatus(parameters.get(STATUS)).getCristinStatus(),
                StandardCharsets.UTF_8);
    }

    private String getUnitIdFromOrganization(String organizationId) {
        return extractLastPathElement(URI.create(organizationId));
    }

    protected URI generateGetProjectUri(String id, String language) throws URISyntaxException {
        return CristinQuery.fromIdAndLanguage(id, language);
    }

    private List<CristinProject> mapValidResponsesToCristinProjects(List<HttpResponse<String>> responses) {
        return responses.stream()
                .map(attempt(response -> getDeserializedResponse(response, CristinProject.class)))
                .map(Try::orElseThrow)
                .filter(CristinProject::hasValidContent)
                .collect(Collectors.toList());
    }

    private BadGatewayException projectHasNotValidContent(String id) {
        logger.warn(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
        return new BadGatewayException(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
    }

    private List<NvaProject> mapValidCristinProjectsToNvaProjects(List<CristinProject> cristinProjects) {
        return cristinProjects.stream()
                .filter(CristinProject::hasValidContent)
                .map(CristinProject::toNvaProject)
                .collect(Collectors.toList());
    }

    private Map<String, String> rewrapOrganizationUri(Map<String, String> requestQueryParameters) {
        if (requestQueryParameters.containsKey(ORGANIZATION)) {
            String organizationId = requestQueryParameters.get(ORGANIZATION);
            requestQueryParameters.put(ORGANIZATION, URLEncoder.encode(organizationId, StandardCharsets.UTF_8));
        }
        return requestQueryParameters;
    }

    private URI getServiceUri(Map<String, String> queryParameters) {
        final String identifier = queryParameters.remove(CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID);
        return new UriWrapper(HTTPS,
                DOMAIN_NAME).addChild(BASE_PATH)
                .addChild(ORGANIZATION_PATH)
                .addChild(identifier)
                .addChild(PROJECTS_PATH)
                .addQueryParameters(queryParameters)
                .getUri();
    }

}
