package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.databind.JsonNode;
import no.unit.nva.cristin.organization.dto.InstitutionBaseDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.exception.FailedHttpRequestException;
import no.unit.nva.cristin.organization.exception.HttpClientFailureException;
import no.unit.nva.cristin.organization.exception.NonExistingUnitError;
import no.unit.nva.cristin.organization.utils.InstitutionUtils;
import no.unit.nva.cristin.organization.utils.Language;
import no.unit.nva.cristin.organization.utils.UriUtils;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public static final String PARENT_UNIT_URI_TEMPLATE =
        "https://api.cristin.no/v2/units?parent_unit_id=%s&per_page" + "=20000";
    public static final int FIRST_EFFORT = 0;
    public static final int MAX_EFFORTS = 2;
    public static final int WAITING_TIME = 500; //500 milliseconds
    public static final String LOG_INTERRUPTION = "InterruptedException while waiting to resend HTTP request";
    private final HttpClient httpClient;

    private static final Logger logger = LoggerFactory.getLogger(HttpExecutorImpl.class);

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
    public InstitutionListResponse getInstitutions(Language language) throws HttpClientFailureException {

        return attempt(() -> URI.create(generateInstitutionsQueryUri(language)))
            .flatMap(this::sendRequestMultipleTimes)
            .map(this::throwExceptionIfNotSuccessful)
            .map(HttpResponse::body)
            .map(this::toInstitutionListResponse)
            .orElseThrow(this::handleError);
    }

    @Override
    public JsonNode getNestedInstitution(URI uri, Language language)
        throws HttpClientFailureException, FailedHttpRequestException {
        URI unitUri = getInstitutionUnitUri(uri, language);

        InstitutionBaseDto institutionUnit = getInstitutionBaseDto(unitUri, language);
        List<SubSubUnitDto> subSubUnitDtoResponses = fetchInstitutionUnits(language, institutionUnit);

        NestedInstitutionGenerator generator = new NestedInstitutionGenerator();
        generator.setInstitution(institutionUnit);
        addSubSubUnitsToNestedInstitutionGenerator(generator, subSubUnitDtoResponses);

        return generator.getNestedInstitution();
    }

    private List<SubSubUnitDto> fetchInstitutionUnits(Language language, InstitutionBaseDto institutionUnit)
        throws HttpClientFailureException, FailedHttpRequestException {
        List<URI> unitUris = getUnitUris(institutionUnit.getId(), language);
        List<Try<SubSubUnitDto>> subSubUnitDtoResponses = fetchSubSubUnitInformation(language, unitUris);
        checkForRequestFailures(subSubUnitDtoResponses);
        return subSubUnitDtoResponses.stream().map(Try::get).collect(Collectors.toList());
    }

    private List<URI> getUnitUris(String id, Language language) throws HttpClientFailureException {
        return
            attempt(
                () -> UriUtils.getUriWithLanguage(URI.create(String.format(PARENT_UNIT_URI_TEMPLATE, id)), language))
                .flatMap(this::sendRequestMultipleTimes)
                .map(this::throwExceptionIfNotSuccessful)
                .map(HttpResponse::body)
                .map(this::bodyToUriList)
                .orElseThrow(this::handleError);
    }

    private void addSubSubUnitsToNestedInstitutionGenerator(NestedInstitutionGenerator generator,
                                                            List<SubSubUnitDto> subSubUnitDtoResponses) {
        subSubUnitDtoResponses
            .forEach(subsubUnit -> generator.addUnitToModel(subsubUnit.getSourceUri(), subsubUnit));
    }

    private List<Try<SubSubUnitDto>> fetchSubSubUnitInformation(Language language, List<URI> unitUris) {
        return unitUris.stream().parallel()
            .map(attempt(subSubUnitUri -> getSubSubUnitDtoWithMultipleEfforts(subSubUnitUri, language)))
            .collect(Collectors.toList());
    }

    private void checkForRequestFailures(List<Try<SubSubUnitDto>> subsubUnitDtoResponses)
        throws FailedHttpRequestException {
        Optional<Try<SubSubUnitDto>> failedRequest = subsubUnitDtoResponses
            .stream().parallel()
            .filter(Try::isFailure)
            .findAny();
        if (failedRequest.isPresent()) {
            throw new FailedHttpRequestException(failedRequest.get().getException());
        }
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
    public NvaOrganization getSingleUnit(URI uri, Language language)
        throws InterruptedException, NonExistingUnitError, HttpClientFailureException {
        SingleUnitHierarchyGenerator singleUnitHierarchyGenerator =
            new SingleUnitHierarchyGenerator(uri, language, httpClient);
        SubSubUnitDto subSubUnitDto = singleUnitHierarchyGenerator.fetch(uri);
        return toNvaOrganization(subSubUnitDto);
    }

    private NvaOrganization toNvaOrganization(SubSubUnitDto subSubUnitDto) {
        NvaOrganization nvaOrganization =  new NvaOrganization();
        nvaOrganization.setId(buildUri(CRISTIN_API_BASE_URL, INSTITUTION_PATH,
                subSubUnitDto.getId()));
        nvaOrganization.setName(subSubUnitDto.getUnitName());
        return nvaOrganization;
    }

    public URI getInstitutionUnitUri(URI uri, Language language) throws HttpClientFailureException {
        InstitutionBaseDto institutionDto = getInstitutionBaseDto(uri, language);
        return institutionDto.getCorrespondingUnitDto().getUri();
    }

    private SubSubUnitDto getSubSubUnitDtoWithMultipleEfforts(URI subunitUri, Language language)
        throws HttpClientFailureException {

        SubSubUnitDto subsubUnitDto = Try.of(UriUtils.getUriWithLanguage(subunitUri, language))
            .flatMap(this::sendRequestMultipleTimes)
            .map(this::throwExceptionIfNotSuccessful)
            .map(HttpResponse::body)
            .map(InstitutionUtils::toSubSubUnitDto)
            .orElseThrow(this::handleError);

        subsubUnitDto.setSourceUri(subunitUri);
        return subsubUnitDto;
    }

    private List<URI> bodyToUriList(String json) throws IOException {
        return InstitutionUtils.toUriList(json);
    }

    private InstitutionBaseDto getInstitutionBaseDto(URI uri, Language language) throws HttpClientFailureException {
        InstitutionBaseDto institutionBaseDto = attempt(() -> UriUtils.getUriWithLanguage(uri, language))
            .flatMap(this::sendRequestMultipleTimes)
            .map(this::throwExceptionIfNotSuccessful)
            .map(HttpResponse::body)
            .map(this::toInstitutionBaseDto)
            .orElseThrow(this::handleError);
        institutionBaseDto.setSourceUri(uri);
        return institutionBaseDto;
    }

    private InstitutionBaseDto toInstitutionBaseDto(String json) throws IOException {
        return InstitutionUtils.toInstitutionBaseDto(json);
    }

    private <T> HttpClientFailureException handleError(Failure<T> failure) {
        return new HttpClientFailureException(failure.getException(), failure.getException().getMessage());
    }

    private InstitutionListResponse toInstitutionListResponse(String institutionDto) throws IOException {
        return InstitutionUtils.toInstitutionListResponse(institutionDto);
    }

    private String generateInstitutionsQueryUri(Language language) {
        return String.format(INSTITUTIONS_URI_TEMPLATE, language.getCode());
    }
}
