package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.dto.SubUnitDto;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.core.attempt.Try.attempt;


public class HttpExecutorImpl {

    public static final int FIRST_EFFORT = 0;
    public static final int MAX_EFFORTS = 2;
    public static final int WAITING_TIME = 500; //500 milliseconds
    public static final String LOG_INTERRUPTION = "InterruptedException while waiting to resend HTTP request";
    public static final String ERROR_MESSAGE_FORMAT = "%d:%s";
    private static final Logger logger = LoggerFactory.getLogger(HttpExecutorImpl.class);
    public static int FIRST_NON_SUCCESSFUL_CODE = HTTP_MULT_CHOICE;
    public static int FIRST_SUCCESSFUL_CODE = HTTP_OK;
    public static String NULL_HTTP_RESPONSE_ERROR_MESSAGE = "No HttpResponse found";
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
        this.httpClient = client;
    }

    public Organization getOrganization(URI uri) throws ApiGatewayException {
        return fromSubSubunit(getSubSubUnitDtoWithMultipleEfforts(uri));
    }

    protected SearchResponse<Organization> query(URI uri) throws NotFoundException, FailedHttpRequestException {
        HttpResponse<String> response = sendRequestMultipleTimes(addLanguage(uri)).get();
        if (isSuccessful(response.statusCode())) {
            try {
                List<Organization> organizations = getOrganizations(response);
                return new SearchResponse<Organization>(uri)
                        .withHits(organizations)
                        .withSize(getCount(response, organizations));
            } catch (JsonProcessingException e) {
                throw new FailedHttpRequestException(e.getMessage());
            }
        } else {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, uri));
        }
    }


    private Organization getParentOrganization(SubSubUnitDto subSubUnitDto) {
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent)
                ? null
                : Set.of(getParentOrganization(attempt(() ->
                getSubSubUnitDtoWithMultipleEfforts(parent)).orElseThrow()));
        return new Organization.Builder()
                .withId(getNvaApiId(subSubUnitDto.getId(), ORGANIZATION_PATH))
                .withPartOf(partOf)
                .withName(subSubUnitDto.getUnitName()).build();
    }

    private Organization fromSubSubunit(SubSubUnitDto subSubUnitDto) {
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent)
                ? null
                : Set.of(getParentOrganization(attempt(() ->
                getSubSubUnitDtoWithMultipleEfforts(parent)).orElseThrow()));
        return new Organization.Builder()
                .withId(getNvaApiId(subSubUnitDto.getId(), ORGANIZATION_PATH))
                .withPartOf(partOf)
                .withHasPart(getSubUnits(subSubUnitDto))
                .withName(subSubUnitDto.getUnitName()).build();
    }

    private Set<Organization> getSubUnits(SubSubUnitDto subSubUnitDto) {
        List<SubUnitDto> subUnits = subSubUnitDto.getSubUnits();
        if (!isNull(subUnits)) {
            return subUnits.stream()
                    .parallel()
                    .map((SubUnitDto subUnitDto) -> getSubUnitDto(subUnitDto.getUri()))
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    private Organization getSubUnitDto(URI cristinUri) {
        return attempt(() -> getSubOrganization(cristinUri)).orElseThrow();
    }

    private Organization getSubOrganization(URI uri) throws ApiGatewayException {
        return getHasParts(getSubSubUnitDtoWithMultipleEfforts(uri));
    }

    private Organization getHasParts(SubSubUnitDto subSubUnitDto) {
        return new Organization.Builder()
                .withId(getNvaApiId(subSubUnitDto.getId(), ORGANIZATION_PATH))
                .withHasPart(getSubUnits(subSubUnitDto))
                .withName(subSubUnitDto.getUnitName()).build();
    }

    private int getCount(HttpResponse<String> response, List<Organization> organizations) {
        return response.headers().firstValue("X-Total-Count").isPresent()
                ? Integer.parseInt(response.headers().firstValue("X-Total-Count").get())
                : organizations.size();
    }

    private List<Organization> getOrganizations(HttpResponse<String> response) throws JsonProcessingException {
        List<SubUnitDto> units = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() { });
        return units.stream()
                .parallel()
                .map(SubUnitDto::getUri)
                .map(uri -> attempt(() -> getOrganization(uri)).orElseThrow())
                .collect(Collectors.toList());
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode <= HTTP_MULT_CHOICE && statusCode >= HTTP_OK;
    }

    private <T> ApiGatewayException handleError(Failure<T> failure) {
        final ApiGatewayException failureException = (ApiGatewayException) failure.getException();
        if (failureException instanceof NotFoundException) {
            return failureException;
        }
        return new FailedHttpRequestException(failureException, failureException.getStatusCode());
    }

    protected Try<HttpResponse<String>> sendRequestMultipleTimes(URI uri) {
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
                .uri(uri)
                .build();
        return httpClient.sendAsync(httpRequest, BodyHandlers.ofString());
    }

    private boolean shouldTryMoreTimes(int effortCount) {
        return effortCount < MAX_EFFORTS;
    }

    @SuppressWarnings("PMD.UselessParentheses") // keep the parenthesis for clarity
    private boolean shouldKeepTrying(int effortCount, Try<HttpResponse<String>> lastEffort) {
        return lastEffort == null || (lastEffort.isFailure() && shouldTryMoreTimes(effortCount));
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

    protected SubSubUnitDto getSubSubUnitDtoWithMultipleEfforts(URI subunitUri)
            throws ApiGatewayException {

        SubSubUnitDto subsubUnitDto = Try.of(subunitUri)
                .flatMap(this::sendRequestMultipleTimes)
                .map((HttpResponse<String> response) -> throwExceptionIfNotSuccessful(response, subunitUri))
                .map(HttpResponse::body)
                .map(SubSubUnitDto::fromJson)
                .orElseThrow(this::handleError);

        subsubUnitDto.setSourceUri(subunitUri);
        return subsubUnitDto;
    }

    protected HttpResponse<String> throwExceptionIfNotSuccessful(HttpResponse<String> response, URI requestedUri)
            throws FailedHttpRequestException, NotFoundException {
        if (isNull(response)) {
            throw new FailedHttpRequestException(NULL_HTTP_RESPONSE_ERROR_MESSAGE);
        } else if (response.statusCode() >= FIRST_SUCCESSFUL_CODE
                && response.statusCode() < FIRST_NON_SUCCESSFUL_CODE) {
            return response;
        } else if (response.statusCode() == HTTP_NOT_FOUND) {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, requestedUri));
        } else {
            throw new FailedHttpRequestException(errorMessage(response));
        }
    }

    private String errorMessage(HttpResponse<String> response) {
        return String.format(ERROR_MESSAGE_FORMAT, response.statusCode(), response.body());
    }
}

