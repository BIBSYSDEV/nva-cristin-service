package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.nva.Organization;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.exception.HttpClientFailureException;
import no.unit.nva.cristin.organization.exception.NonExistingUnitError;
import no.unit.nva.cristin.organization.utils.InstitutionUtils;
import no.unit.nva.cristin.organization.utils.Language;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static no.unit.nva.cristin.common.util.UriUtils.buildUri;
import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_BASE_URL;
import static no.unit.nva.cristin.projects.Constants.INSTITUTION_PATH;
import static nva.commons.core.attempt.Try.attempt;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.USER_AGENT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class HttpExecutorImpl extends HttpExecutor {

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

    @Override
    public OrganizationListResponse getInstitutions(Language language) throws HttpClientFailureException {

        return attempt(() -> URI.create(generateInstitutionsQueryUri(language)))
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
                .header(ACCEPT, APPLICATION_JSON.getMimeType())
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
    public Organization getSingleUnit(URI uri, Language language)
            throws InterruptedException, NonExistingUnitError, HttpClientFailureException {
        SingleUnitHierarchyGenerator singleUnitHierarchyGenerator =
                new SingleUnitHierarchyGenerator(uri, language, httpClient);
        SubSubUnitDto subSubUnitDto = singleUnitHierarchyGenerator.fetch(uri, language);
        return toNvaOrganization(subSubUnitDto);
    }

    private Organization toNvaOrganization(SubSubUnitDto subSubUnitDto) {
        Organization nvaOrganization = new Organization.Builder()
                .withId(buildUri(CRISTIN_API_BASE_URL, INSTITUTION_PATH,
                subSubUnitDto.getId()))
                .withName(subSubUnitDto.getUnitName()).build();
        return nvaOrganization;
    }

    private <T> HttpClientFailureException handleError(Failure<T> failure) {
        return new HttpClientFailureException(failure.getException(), failure.getException().getMessage());
    }

    private OrganizationListResponse toInstitutionListResponse(String institutionDto) throws IOException {
        return InstitutionUtils.toInstitutionListResponse(institutionDto);
    }

    private String generateInstitutionsQueryUri(Language language) {
        return String.format(INSTITUTIONS_URI_TEMPLATE, language.getCode());
    }
}
