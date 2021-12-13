package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.dto.SubUnitDto;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.core.attempt.Try.attempt;


public class HttpExecutorImpl {

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

    private Organization getParentOrganization(SubSubUnitDto subSubUnitDto) {
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent)
                ? null
                : Set.of(getParentOrganization(attempt(() -> fetch(parent)).orElseThrow()));
        return new Organization.Builder()
                .withId(getNvaApiId(subSubUnitDto.getId(), ORGANIZATION_PATH))
                .withPartOf(partOf)
                .withName(subSubUnitDto.getUnitName()).build();
    }

    private Organization fromSubSubunit(SubSubUnitDto subSubUnitDto) {
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent)
                ? null
                : Set.of(getParentOrganization(attempt(() -> fetch(parent)).orElseThrow()));
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
                    .map((SubUnitDto subUnitDto) -> getSubUnitDto(subUnitDto.getUri()))
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    private Organization getSubUnitDto(URI cristinUri)  {
        return attempt(() ->  getSubOrganization(cristinUri)).orElseThrow();
    }

    public Organization getSubOrganization(URI uri) throws NotFoundException {
        return getHasParts(fetch(uri));
    }

    private Organization getHasParts(SubSubUnitDto subSubUnitDto) {
        return new Organization.Builder()
                .withId(getNvaApiId(subSubUnitDto.getId(), ORGANIZATION_PATH))
                .withHasPart(getSubUnits(subSubUnitDto))
                .withName(subSubUnitDto.getUnitName()).build();
    }

    public Organization getOrganization(URI uri) throws NotFoundException {
        return fromSubSubunit(fetch(uri));
    }

    protected SubSubUnitDto fetch(URI uri) throws NotFoundException {
        HttpRequest httpRequest = createHttpRequest(addLanguage(uri));
        HttpResponse<String> response = sendRequest(httpRequest);
        if (isSuccessful(response.statusCode())) {
            return SubSubUnitDto.fromJson(response.body());
        } else {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, uri));
        }
    }

    protected SearchResponse<Organization> query(URI uri) throws NotFoundException, FailedHttpRequestException {
        HttpResponse<String> response = sendRequest(createHttpRequest(addLanguage(uri)));
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

    private int getCount(HttpResponse<String> response, List<Organization> organizations) {
        final int count = response.headers().firstValue("X-Total-Count").isPresent()
                ? Integer.parseInt(response.headers().firstValue("X-Total-Count").get())
                : organizations.size();
        return count;
    }

    private List<Organization> getOrganizations(HttpResponse<String> response) throws JsonProcessingException {
        List<SubUnitDto> units = OBJECT_MAPPER.readValue(response.body(), new TypeReference<List<SubUnitDto>>() {
        });
        return units.stream()
                .map(SubUnitDto::getUri)
                .map(uri -> attempt(() -> getOrganization(uri)).orElseThrow())
                .collect(Collectors.toList());
    }

    private HttpRequest createHttpRequest(URI uri) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
    }

    protected HttpResponse<String> sendRequest(HttpRequest httpRequest) {
        return attempt(() -> httpClient.sendAsync(httpRequest, BodyHandlers.ofString()).get()).orElseThrow();
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode <= HTTP_MULT_CHOICE && statusCode >= HTTP_OK;
    }

}
