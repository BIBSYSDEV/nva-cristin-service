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
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.attempt.Failure;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.util.Objects.isNull;
import static no.unit.nva.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.CRISTIN_QUERY_NAME_PARAM;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.Constants.UNIT_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.attempt.Try.of;

@SuppressWarnings("PMD.GodClass")
public class CristinOrganizationApiClient extends ApiClient {

    public static final String CRISTIN_LEVELS_PARAM = "levels";
    public static final String ERROR_MESSAGE_FORMAT = "%d:%s";
    public static final String NULL_HTTP_RESPONSE_ERROR_MESSAGE = "No HttpResponse found";
    public static final int SINGLE_HIT = 1;
    public static final String UNIQUELY_IDENTIFY_ORGANIZATION = "Identifier does not uniquely identify organization";
    public static final String FIRST_LEVEL = "1";
    public static final String ALL_SUB_LEVELS = "32";
    public static final int FIRST_AND_ONLY_UNIT = 0;
    private static final int NO_HITS = 0;

    /**
     * Create a CristinOrganizationApiClient with default HTTPClient.
     */
    public CristinOrganizationApiClient() {
        this(defaultHttpClient());
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
    public Organization getOrganization(URI uri) throws ApiGatewayException {
        return fromSubSubunit(getSubSubUnitDtoWithMultipleEfforts(uri));
    }

    /**
     * Get information for an Organization without parent of subunits.
     *
     * @param identifier the Cristin unit URI
     * @return an {@link Organization} containing the information
     */
    public Organization getFlatOrganization(String identifier) throws ApiGatewayException {
        URI cristinUri = getCristinOrganizationByIdentifierUri(identifier);
        HttpResponse<String> response = sendRequestMultipleTimes(cristinUri).get();
        return isSuccessful(response.statusCode()) ? extractOrganization(identifier, response) : null;
    }

    private Organization extractOrganization(String identifier, HttpResponse<String> response)
            throws ApiGatewayException {
        List<SubUnitDto> units = attempt(() ->
                OBJECT_MAPPER.readValue(response.body(), new TypeReference<List<SubUnitDto>>() {
                }))
                .orElseThrow(fail -> new FailedHttpRequestException(fail.getException()));
        if (SINGLE_HIT == units.size()) {
            return toOrganization(identifier, units.get(FIRST_AND_ONLY_UNIT));
        } else {
            throw new BadRequestException(UNIQUELY_IDENTIFY_ORGANIZATION);
        }
    }

    private Organization toOrganization(String identifier, SubUnitDto subUnitDto) {
        return new Organization.Builder()
                   .withId(getNvaApiId(identifier, ORGANIZATION_PATH))
                   .withName(subUnitDto.getName())
                   .withLabels(subUnitDto.getName())
                   .withAcronym(subUnitDto.getAcronym())
                   .build();
    }

    private URI getCristinOrganizationByIdentifierUri(String identifier) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(UNITS_PATH)
                .addQueryParameter(UNIT_ID, identifier)
                .getUri();
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
        return TOP.equals(depth) || isNull(depth) ? FIRST_LEVEL : ALL_SUB_LEVELS;
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
        int firstPage = Integer.parseInt(requestQueryParams.get(PAGE)) - SINGLE_HIT;
        return firstPage > NO_HITS && totalSize > NO_HITS ? getUri(requestQueryParams, firstPage, baseUri) : null;
    }

    private URI nextResult(URI baseUri, Map<String, String> requestQueryParams, int totalSize) {
        int currentPage = Integer.parseInt(requestQueryParams.get(PAGE));
        int pageSize = Integer.parseInt(requestQueryParams.get(NUMBER_OF_RESULTS));
        return currentPage * pageSize < totalSize
                ? getUri(requestQueryParams, currentPage + SINGLE_HIT, baseUri)
                : null;
    }

    private URI getUri(Map<String, String> requestQueryParams, int firstPage, URI baseUri) {
        Map<String, String> nextMap = new ConcurrentHashMap<>(requestQueryParams);
        nextMap.put(PAGE, Integer.toString(firstPage));
        return UriWrapper.fromUri(baseUri).addQueryParameters(nextMap).getUri();
    }

    protected SearchResponse<Organization> query(URI uri) throws NotFoundException, FailedHttpRequestException {
        HttpResponse<String> response = sendRequestMultipleTimes(uri).get();
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
                   .withName(subSubUnitDto.getUnitName())
                   .withLabels(subSubUnitDto.getUnitName())
                   .build();
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
                   .withName(subSubUnitDto.getUnitName())
                   .withLabels(subSubUnitDto.getUnitName())
                   .build();
    }

    private Set<Organization> getSubUnits(SubSubUnitDto subSubUnitDto) {
        List<SubUnitDto> subUnits = subSubUnitDto.getSubUnits();
        return !isNull(subUnits) ? subUnits.stream()
                .parallel()
                .map((SubUnitDto subUnitDto) -> getSubUnitDto(subUnitDto.getUri()))
                .collect(Collectors.toSet()) : null;
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
                   .withName(subSubUnitDto.getUnitName())
                   .withLabels(subSubUnitDto.getUnitName())
                   .build();
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

        SubSubUnitDto subsubUnitDto = of(subunitUri)
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
