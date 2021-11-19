package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.utils.InstitutionUtils;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonUtils;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.USER_AGENT;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.ALL_QUERY_PARAMETER_LANGUAGES;
import static no.unit.nva.cristin.model.Constants.APPLICATION_JSON;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.QUERY_PARAMETER_LANGUAGE;
import static nva.commons.core.attempt.Try.attempt;


public class HttpExecutorImpl extends HttpExecutor {

    public static final String PARSE_ERROR = "Failed to parse: ";
    public static final String NVA_INSTITUTIONS_LIST_CRAWLER = "NVA Institutions List Crawler";
    public static final String INSTITUTIONS_URI_TEMPLATE =
            "https://api.cristin.no/v2/institutions?country=NO" + "&per_page=1000&lang=%s&cristin_institution=true";
    public static final int FIRST_EFFORT = 0;
    public static final int MAX_EFFORTS = 2;
    public static final int WAITING_TIME = 500; //500 milliseconds
    public static final String LOG_INTERRUPTION = "InterruptedException while waiting to resend HTTP request";
    private static final Logger logger = LoggerFactory.getLogger(HttpExecutorImpl.class);
    private final transient HttpClient httpClient;

    /**
     * Default constructor.
     */
    @JacocoGenerated
    public HttpExecutorImpl() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    public HttpExecutorImpl(HttpClient client) {
        super();
        this.httpClient = client;
    }

    public static URI getUriWithLanguage(URI uri) {
        return new UriWrapper(uri).addQueryParameter(QUERY_PARAMETER_LANGUAGE, ALL_QUERY_PARAMETER_LANGUAGES).getUri();
    }

    @Override
    public SearchResponse<Organization> getInstitutions() throws FailedHttpRequestException {

        return attempt(() -> URI.create(generateInstitutionsQueryUri()))
                .flatMap(this::sendRequestMultipleTimes)
                .map(this::throwExceptionIfNotSuccessful)
                .map(HttpResponse::body)
                .map(this::toInstitutionListResponse)
                .orElseThrow(this::handleError);
    }

    private Try<HttpResponse<String>> sendRequestMultipleTimes(URI uri) {
        Try<HttpResponse<String>> lastEffort = null;
        for (int effortCount = FIRST_EFFORT; shouldKeepTrying(effortCount, lastEffort); effortCount++) {
            waitBeforeRetrying(effortCount);
            lastEffort = attemptFetch(uri, effortCount);
        }
        return lastEffort;
    }

    private Try<HttpResponse<String>> attemptFetch(URI uri, int effortCount) {
        Try<HttpResponse<String>> newEffort = attempt(() -> createAndSendHttpRequest(uri).get());
        if (newEffort.isFailure()) {
            logger.warn(String.format("Failed HttpRequest on attempt %d of 3: ", effortCount + 1)
                    + newEffort.getException().getMessage(), newEffort.getException()
            );
        }
        return newEffort;
    }

    private CompletableFuture<HttpResponse<String>> createAndSendHttpRequest(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .header(ACCEPT, APPLICATION_JSON)
                .header(USER_AGENT, NVA_INSTITUTIONS_LIST_CRAWLER)
                .uri(uri)
                .build();
        return httpClient.sendAsync(httpRequest, BodyHandlers.ofString());
    }

    private int waitBeforeRetrying(int effortCount) {
        if (effortCount > FIRST_EFFORT) {
            try {
                Thread.sleep(WAITING_TIME);
            } catch (InterruptedException e) {
                logger.error(LOG_INTERRUPTION);
                throw new RuntimeException(e);
            }
        }
        return effortCount;
    }

    @SuppressWarnings("PMD.UselessParentheses") // keep the parenthesis for clarity
    private boolean shouldKeepTrying(int effortCount, Try<HttpResponse<String>> lastEffort) {
        return lastEffort == null || (lastEffort.isFailure() && shouldTryMoreTimes(effortCount));
    }

    private boolean shouldTryMoreTimes(int effortCount) {
        return effortCount < MAX_EFFORTS;
    }

    @Override
    public Organization getSingleUnit(URI uri)
            throws InterruptedException, NotFoundException, FailedHttpRequestException {

        SubSubUnitDto subSubUnitDto = fetch(uri);
        return toOrganization(subSubUnitDto);
    }

    private Organization toOrganization(SubSubUnitDto subSubUnitDto) {
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent) ? Collections.emptySet() : Set.of(toOrganization(wrapFetching(parent)));
        return new Organization.Builder()
                .withId(new UriWrapper(HTTPS,
                        DOMAIN_NAME).addChild(BASE_PATH)
                        .addChild(ORGANIZATION_PATH)
                        .addChild(subSubUnitDto.getId())
                        .getUri())
                .withPartOf(partOf)
                .withName(subSubUnitDto.getUnitName()).build();
    }

    private <T> FailedHttpRequestException handleError(Failure<T> failure) {
        return new FailedHttpRequestException(failure.getException(), failure.getException().getMessage());
    }

    private SearchResponse<Organization> toInstitutionListResponse(String institutionDto) throws IOException {
        return InstitutionUtils.toInstitutionListResponse(institutionDto);
    }

    private String generateInstitutionsQueryUri() {
        return INSTITUTIONS_URI_TEMPLATE;
    }

    @Override
    public Organization getNestedInstitution(URI uri)
            throws NotFoundException, FailedHttpRequestException, InterruptedException {
        SubSubUnitDto currentUnit = fetch(getUriWithLanguage(uri));

//        List<SubSubUnitDto> list = currentUnit.getParentUnits().stream()
//                .map(this::getUnitUri)
//                .filter(Objects::nonNull)
//                .map(this::wrapFetching)
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//        System.out.println(list);

        Organization currentOrganization = toOrganization(currentUnit);
        return currentOrganization;
    }

    private SubSubUnitDto wrapFetching(URI uri) {
        try {
            return fetch(uri);
        } catch (FailedHttpRequestException | InterruptedException | NotFoundException e) {
            return null;
        }
    }

    private SubSubUnitDto fetch(URI uri) throws InterruptedException, NotFoundException, FailedHttpRequestException {
        logger.info("Fetching " + uri.toString());
        HttpRequest httpRequest = createHttpRequest(uri);
        HttpResponse<String> response = sendRequest(httpRequest);
        if (isSuccessful(response.statusCode())) {
            return toUnit(response.body());
        } else {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, uri));
        }
    }

//    private SubSubUnitDto fetchWithRetry(URI subunitUri) throws FailedHttpRequestException {
//
//        SubSubUnitDto subsubUnitDto = Try.of(getUriWithLanguage(subunitUri))
//                .flatMap(this::sendRequestMultipleTimes)
//                .map(this::throwExceptionIfNotSuccessful)
//                .map(HttpResponse::body)
//                .map(this::toUnit)
//                .orElseThrow(this::handleError);
//
//        subsubUnitDto.setSourceUri(subunitUri);
//        return subsubUnitDto;
//    }

    private HttpRequest createHttpRequest(URI uri) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
    }

    private HttpResponse<String> sendRequest(HttpRequest httpRequest) throws InterruptedException,
            FailedHttpRequestException {
        try {
            return httpClient.sendAsync(httpRequest, BodyHandlers.ofString()).get();
        } catch (ExecutionException e) {
            throw new FailedHttpRequestException(e);
        }
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode <= HTTP_MULT_CHOICE && statusCode >= HTTP_OK;
    }

    private SubSubUnitDto toUnit(String json) {
        try {
            return JsonUtils.dtoObjectMapper.readValue(json, SubSubUnitDto.class);
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON string: " + json, e);
        }
        return null;
    }

}
