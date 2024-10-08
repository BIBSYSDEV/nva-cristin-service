package no.unit.nva.cristin.organization.common.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.client.FetchApiClient;
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
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.Constants.UNIT_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.organization.common.QueryParamConverter.translateToCristinApi;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.attempt.Try.of;

@SuppressWarnings("PMD.GodClass")
public class CristinOrganizationApiClient
    extends ApiClient
    implements CristinQueryApiClient<Map<String, String>, Organization>,
               FetchApiClient<Map<String, String>, Organization> {

    public static final String ERROR_MESSAGE_FORMAT = "%d:%s";
    public static final String NULL_HTTP_RESPONSE_ERROR_MESSAGE = "No HttpResponse found";
    public static final int SINGLE_HIT = 1;
    public static final String UNIQUELY_IDENTIFY_ORGANIZATION = "Identifier does not uniquely identify organization";
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

    @Override
    public SearchResponse<Organization> executeQuery(Map<String, String> params) throws ApiGatewayException {
        return queryOrganizations(params);
    }

    @Override
    public Organization executeFetch(Map<String, String> params) throws ApiGatewayException {
        if (NONE.equals(params.get(DEPTH))) {
            var organization = getFlatOrganization(params.get(IDENTIFIER));
            organization.setContext(ORGANIZATION_CONTEXT);
            return organization;
        } else {
            var cristinUri = getCristinUri(params.get(IDENTIFIER), UNITS_PATH);
            var organization = getOrganization(cristinUri);
            organization.setContext(ORGANIZATION_CONTEXT);
            return organization;
        }
    }

    private Organization extractOrganization(String identifier, HttpResponse<String> response)
            throws ApiGatewayException {
        var type = new TypeReference<List<SubUnitDto>>() {};
        List<SubUnitDto> units = attempt(() -> OBJECT_MAPPER.readValue(response.body(), type))
                .orElseThrow(fail -> new FailedHttpRequestException(fail.getException()));
        if (SINGLE_HIT == units.size()) {
            return toOrganization(identifier, units.getFirst());
        } else {
            throw new BadRequestException(UNIQUELY_IDENTIFY_ORGANIZATION);
        }
    }

    private Organization toOrganization(String identifier, SubUnitDto subUnitDto) {
        return new Organization.Builder()
                   .withId(getNvaApiId(identifier, ORGANIZATION_PATH))
                   .withLabels(subUnitDto.getName())
                   .withAcronym(subUnitDto.getAcronym())
                   .withCountry(subUnitDto.getCountry())
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
        throws ApiGatewayException {

        URI queryUri = createCristinQueryUri(translateToCristinApi(requestQueryParams), UNITS_PATH);
        final long start = System.currentTimeMillis();
        SearchResponse<Organization> searchResponse = query(queryUri);
        final long totalProcessingTime = System.currentTimeMillis() - start;
        return updateSearchResponseMetadata(searchResponse, requestQueryParams, totalProcessingTime);
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
                   .withLabels(subSubUnitDto.getUnitName())
                   .withCountry(subSubUnitDto.getCountry())
                   .withAcronym(subSubUnitDto.getAcronym())
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

    /**
     * Sends calls to upstream with multiple retries.
     */
    public SubSubUnitDto getSubSubUnitDtoWithMultipleEfforts(URI subunitUri) throws ApiGatewayException {

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
            var resource = extractLastPathElement(requestedUri);
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, resource));
        } else {
            throw new FailedHttpRequestException(errorMessage(response));
        }
    }

    private String errorMessage(HttpResponse<String> response) {
        return String.format(ERROR_MESSAGE_FORMAT, response.statusCode(), response.body());
    }

}
