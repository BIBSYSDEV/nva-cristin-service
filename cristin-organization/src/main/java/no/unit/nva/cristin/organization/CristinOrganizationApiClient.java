package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.dto.SubUnitDto;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;

public class CristinOrganizationApiClient extends ApiClient {

    public static final String CRISTIN_LEVELS_PARAM = "levels";
    public static final String CRISTIN_PER_PAGE_PARAM = "per_page";
    public static final String ERROR_MESSAGE_FORMAT = "%d:%s";
    public static final String NULL_HTTP_RESPONSE_ERROR_MESSAGE = "No HttpResponse found";
    private static final String CRISTIN_QUERY_NAME_PARAM = "name";

    /**
     * Create a CristinOrganizationApiClient with default HTTPClient.
     */
    public CristinOrganizationApiClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    public CristinOrganizationApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Get information for an Organization.
     *
     * @param uri the Cristin unit URI
     * @return an {@link Organization} containing the information
     * @throws NotFoundException when the URI does not correspond to an existing unit.
     */
    public Organization getOrganization(URI uri) throws ApiGatewayException  {
        return fromSubSubunit(getSubSubUnitDtoWithMultipleEfforts(uri));
    }

    /**
     * Fetch Organizations matching given query criteria.
     *
     * @param requestQueryParams Map containing verified query parameters
     */
    public SearchResponse<Organization> queryOrganizations(Map<String, String> requestQueryParams)
            throws NotFoundException, FailedHttpRequestException {

        URI queryUri = createCristinQueryUri(translateToCristinApi(requestQueryParams), UNITS_PATH);
        final long start = System.currentTimeMillis();
        SearchResponse<Organization> searchResponse = query(queryUri);
        final long totalProcessingTime = System.currentTimeMillis() - start;
        return updateSearchResponseMetadata(searchResponse, requestQueryParams, totalProcessingTime);
    }

    private Map<String, String> translateToCristinApi(Map<String, String> requestQueryParams) {
        return Map.of(
                CRISTIN_LEVELS_PARAM, toCristinLevel(requestQueryParams.get(DEPTH)),
                CRISTIN_QUERY_NAME_PARAM, requestQueryParams.get(QUERY),
                PAGE, requestQueryParams.get(PAGE),
                CRISTIN_PER_PAGE_PARAM, requestQueryParams.get(NUMBER_OF_RESULTS));
    }

    private String toCristinLevel(String depth) {
        return TOP.equals(depth) || isNull(depth) ? "1" : "32";
    }

    private SearchResponse<Organization> updateSearchResponseMetadata(
            SearchResponse<Organization> searchResponse,
            Map<String, String> requestQueryParams,
            long timeUsed) {
        searchResponse.setContext(ORGANIZATION_CONTEXT);
        searchResponse.setId(createIdUriFromParams(requestQueryParams, ORGANIZATION_PATH));
        if (searchResponse.isNotEmpty()) {
            searchResponse.setFirstRecord(calculateFirstRecord(requestQueryParams));
        }
        searchResponse.setNextResults(nextResult(getNvaApiUri(ORGANIZATION_PATH),
                requestQueryParams,
                searchResponse.getSize()));
        searchResponse.setPreviousResults(previousResult(getNvaApiUri(ORGANIZATION_PATH),
                requestQueryParams,
                searchResponse.getSize()));
        searchResponse.setProcessingTime(timeUsed);
        return searchResponse;
    }


    private URI previousResult(URI baseUri, Map<String, String> requestQueryParams, int totalSize) {
        int firstPage = Integer.parseInt(requestQueryParams.get(PAGE)) - 1;
        return firstPage > 0 && totalSize > 0 ? getUri(requestQueryParams, firstPage, baseUri) : null;
    }

    private URI nextResult(URI baseUri, Map<String, String> requestQueryParams, int totalSize) {
        int currentPage = Integer.parseInt(requestQueryParams.get(PAGE));
        int pageSize = Integer.parseInt(requestQueryParams.get(NUMBER_OF_RESULTS));
        return currentPage * pageSize < totalSize ? getUri(requestQueryParams, currentPage + 1, baseUri) : null;
    }

    private URI getUri(Map<String, String> requestQueryParams, int firstPage, URI baseUri) {
        Map<String, String> nextMap = new ConcurrentHashMap<>(requestQueryParams);
        nextMap.put(PAGE, Integer.toString(firstPage));
        return new UriWrapper(baseUri).addQueryParameters(nextMap).getUri();
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
        } else if (response.statusCode() == HTTP_NOT_FOUND) {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, uri));
        } else {
            throw new FailedHttpRequestException(errorMessage(response));
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

    private List<Organization> getOrganizations(HttpResponse<String> response) throws JsonProcessingException {
        List<SubUnitDto> units = OBJECT_MAPPER.readValue(response.body(), new TypeReference<>() {
        });
        return units.stream()
                .parallel()
                .map(SubUnitDto::getUri)
                .map(uri -> attempt(() -> getOrganization(uri)).orElseThrow())
                .collect(Collectors.toList());
    }


    protected SubSubUnitDto getSubSubUnitDtoWithMultipleEfforts(URI subunitUri) throws ApiGatewayException {

        SubSubUnitDto subsubUnitDto = Try.of(subunitUri)
                .map(UriUtils::addLanguage)
                .flatMap(this::sendRequestMultipleTimes)
                .map((HttpResponse<String> response) -> throwExceptionIfNotSuccessful(response, subunitUri))
                .map(HttpResponse::body)
                .map(SubSubUnitDto::fromJson)
                .orElseThrow(this::handleError);

        subsubUnitDto.setSourceUri(subunitUri);
        return subsubUnitDto;
    }

    private <T> ApiGatewayException handleError(Failure<T> failure) {
        final ApiGatewayException failureException = (ApiGatewayException) failure.getException();
        if (failureException instanceof NotFoundException) {
            return failureException;
        }
        return new FailedHttpRequestException(failureException, failureException.getStatusCode());
    }

    private HttpResponse<String> throwExceptionIfNotSuccessful(HttpResponse<String> response, URI requestedUri)
            throws FailedHttpRequestException, NotFoundException {
        if (isNull(response)) {
            throw new FailedHttpRequestException(NULL_HTTP_RESPONSE_ERROR_MESSAGE);
        } else if (isSuccessful(response.statusCode())) {
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
