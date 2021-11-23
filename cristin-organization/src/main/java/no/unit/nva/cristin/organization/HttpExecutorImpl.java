package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.dto.SubUnitDto;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonUtils;
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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.getNvaApiId;


public class HttpExecutorImpl extends HttpExecutor {

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


      private Organization toOrganization(SubSubUnitDto subSubUnitDto) {
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent) ? null : Set.of(toOrganization(wrapFetching(parent)));
        final URI id = getNvaApiId(subSubUnitDto.getId());
        final Organization organization = new Organization.Builder()
                .withId(id)
                .withPartOf(partOf)
                .withHasPart(getSubUnits(subSubUnitDto))
                .withName(subSubUnitDto.getUnitName()).build();
        return organization;
    }

    private Set<Organization> getSubUnits(SubSubUnitDto subSubUnitDto) {
        List<SubUnitDto> subUnits = subSubUnitDto.getSubUnits();
        if (!isNull(subUnits)) {
            final URI parent = getNvaApiId(subSubUnitDto.getId());
            return subUnits.stream()
                    .map((SubUnitDto subUnitDto) -> toFlatOrganization(subUnitDto, parent))
                    .collect(Collectors.toSet());
        } else {
            return null;
        }
    }

    private Organization toFlatOrganization(SubUnitDto subUnitDto, URI parent) {
        return new Organization.Builder()
                .withId(getNvaApiId(subUnitDto.getId()))
                .withPartOf(Set.of(new Organization.Builder().withId(parent).build()))
                .withName(subUnitDto.getName())
                .build();
    }

    @Override
    public Organization getNestedInstitution(URI uri)
            throws NotFoundException, FailedHttpRequestException, InterruptedException {
        SubSubUnitDto subSubUnitDto = fetch(uri);
        URI parent = Optional.ofNullable(subSubUnitDto.getParentUnit()).map(InstitutionDto::getUri).orElse(null);
        final Set<Organization> partOf = isNull(parent) ? null : Set.of(toOrganization(wrapFetching(parent)));
        final URI id = getNvaApiId(subSubUnitDto.getId());
        final Organization organization = new Organization.Builder()
                .withId(id)
                .withPartOf(partOf)
                .withHasPart(getSubUnits(subSubUnitDto))
                .withName(subSubUnitDto.getUnitName()).build();
        return organization;
    }

    private SubSubUnitDto wrapFetching(URI uri) {
        try {
            return fetch(uri);
        } catch (FailedHttpRequestException | InterruptedException | NotFoundException e) {
            return null;
        }
    }

    private SubSubUnitDto fetch(URI uri) throws InterruptedException, NotFoundException, FailedHttpRequestException {
        HttpRequest httpRequest = createHttpRequest(addLanguage(uri));
        HttpResponse<String> response = sendRequest(httpRequest);
        if (isSuccessful(response.statusCode())) {
            return toSubSubUnitDto(response.body());
        } else {
            throw new NotFoundException(String.format(NOT_FOUND_MESSAGE_TEMPLATE, uri));
        }
    }

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

    private SubSubUnitDto toSubSubUnitDto(String json) {
        try {
            return JsonUtils.dtoObjectMapper.readValue(json, SubSubUnitDto.class);
        } catch (JsonProcessingException e) {
            logger.error("Error processing JSON string: " + json, e);
        }
        return null;
    }
}
