package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.Constants.PAGE;
import static no.unit.nva.cristin.projects.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.projects.Constants.QUESTION_MARK;
import static no.unit.nva.cristin.projects.Constants.TITLE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_READING_RESPONSE_FAIL;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import static no.unit.nva.cristin.projects.UriUtils.queryParameters;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private static final HttpClient client = HttpClient.newHttpClient();

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
     * @param requestQueryParams Request parameters from client containing title and language
     * @return a ProjectsWrapper filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some error happen we should return this to client
     */
    public ProjectsWrapper queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        Map<String, String> requestQueryParams) throws ApiGatewayException {

        long startRequestTime = System.currentTimeMillis();
        HttpResponse<String> response = queryProjects(requestQueryParams);
        List<CristinProject> enrichedProjectsFromResponse =
            getEnrichedProjectsUsingQueryResponse(response, requestQueryParams.get(LANGUAGE));
        List<NvaProject> nvaProjects = mapValidCristinProjectsToNvaProjects(enrichedProjectsFromResponse);
        long endRequestTime = System.currentTimeMillis();

        return new ProjectsWrapper()
            .usingQueryParams(requestQueryParams)
            .usingHeaders(response.headers())
            .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
            .withHits(nvaProjects);
    }

    protected static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    protected HttpResponse<String> queryProjects(Map<String, String> parameters) throws ApiGatewayException {
        URI uri = attempt(() -> generateQueryProjectsUrl(parameters))
            .toOptional(failure ->
                logError(ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED, queryParameters(parameters), failure.getException()))
            .orElseThrow();

        HttpResponse<String> response = fetchQueryResults(uri);

        checkHttpStatusCode(
            buildUri(BASE_URL, QUESTION_MARK + queryParameters(parameters)).toString(),
            response.statusCode());

        return response;
    }

    protected CristinProject getProject(String id, String language) throws ApiGatewayException {
        URI uri = attempt(() -> generateGetProjectUri(id, language))
            .toOptional(failure -> logError(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID, id, failure.getException()))
            .orElseThrow();

        HttpResponse<String> response = fetchGetResult(uri);

        checkHttpStatusCode(buildUri(BASE_URL, id).toString(), response.statusCode());

        return getDeserializedResponse(response, CristinProject.class);
    }

    protected List<CristinProject> getEnrichedProjectsUsingQueryResponse(HttpResponse<String> response,
                                                                         String language)
        throws ApiGatewayException {

        List<CristinProject> projectsFromQuery = asList(getDeserializedResponse(response, CristinProject[].class));

        return projectsFromQuery.stream()
            .map(project -> attempt(() -> getProject(project.getCristinProjectId(), language))
                .toOptional(failure -> logError(
                    ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID,
                    project.getCristinProjectId(),
                    failure.getException()))
                .orElse(project)).collect(Collectors.toList());
    }

    protected URI generateQueryProjectsUrl(Map<String, String> parameters) throws URISyntaxException {
        return new CristinQuery()
            .withTitle(parameters.get(TITLE))
            .withLanguage(parameters.get(LANGUAGE))
            .fromPage(parameters.get(PAGE))
            .toURI();
    }

    protected URI generateGetProjectUri(String id, String language) throws URISyntaxException {
        return CristinQuery.fromIdAndLanguage(id, language);
    }

    @JacocoGenerated
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    @JacocoGenerated
    protected HttpResponse<String> fetchGetResult(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();

        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    private BadGatewayException projectHasNotValidContent(String id) {
        logger.warn(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
        return new BadGatewayException(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
    }

    @JacocoGenerated
    protected HttpResponse<String> fetchQueryResults(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();

        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    private <T> T getDeserializedResponse(HttpResponse<String> response, Class<T> classOfT)
        throws BadGatewayException {

        return attempt(() -> fromJson(response.body(), classOfT)).orElseThrow(failure -> {
            logError(ERROR_MESSAGE_READING_RESPONSE_FAIL, response.body(),
                failure.getException());
            return new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        });
    }

    private void checkHttpStatusCode(String uri, int statusCode)
        throws NotFoundException, BadGatewayException {

        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new NotFoundException(uri);
        } else if (statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        } else if (statusCode >= HttpURLConnection.HTTP_MULT_CHOICE) { // Greater than or equal to 300
            throw new RuntimeException();
        }
    }

    private List<NvaProject> mapValidCristinProjectsToNvaProjects(List<CristinProject> cristinProjects) {
        return cristinProjects.stream()
            .filter(CristinProject::hasValidContent)
            .map(CristinProject::toNvaProject)
            .collect(Collectors.toList());
    }

    private void logError(String message, String data, Exception failure) {
        logger.error(String.format(message, data, failure.getMessage()));
    }
}
