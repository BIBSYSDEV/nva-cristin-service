package no.unit.nva.cristin.organization;


import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.exception.HttpClientFailureException;
import no.unit.nva.exception.NonExistingUnitError;
import no.unit.nva.language.Language;
import nva.commons.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.nonNull;

@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")    // TODO Fix external access
public class SingleUnitHierarchyGenerator {

    private final transient HttpClient httpClient;
    private final transient  Logger logger = LoggerFactory.getLogger(SingleUnitHierarchyGenerator.class);

    /**
     * Parametrized constructor.
     *
     * @param uri        the URI of the Crstin Unit
     * @param language   the language we want the information in.
     * @param httpClient an {@link HttpClient}
     * @throws InterruptedException       when the client throws such exception.
     * @throws NonExistingUnitError       when the URI does not correspond to an existing unit.
     * @throws HttpClientFailureException when HttpClient receives an error.
     */
    public SingleUnitHierarchyGenerator(URI uri, Language language, HttpClient httpClient)
            throws InterruptedException, NonExistingUnitError, HttpClientFailureException {
        this.httpClient = httpClient;
        fetchHierarchy(uri, language);
    }

    private void fetchHierarchy(URI uri, Language language)
            throws InterruptedException, NonExistingUnitError, HttpClientFailureException {
        SubSubUnitDto current = fetchAndUpdateModel(uri, language);
        URI parent = Optional.ofNullable(current.getParentUnit())
                .map(InstitutionDto::getUri).orElse(null);
        while (nonNull(current.getParentUnit())) {
            current = fetchAndUpdateModel(parent, language);
            if (nonNull(current.getParentUnit())) {
                parent = current.getParentUnit().getUri();
            }
        }
    }

    private SubSubUnitDto fetchAndUpdateModel(URI uri, Language language)
            throws InterruptedException, NonExistingUnitError, HttpClientFailureException {
        return this.fetch(uri, language);
    }

    /**
     * Fetches an OOrganization with given id.
     * @param uri id of organization to fetch
     * @param language in which languages information is wanted
     * @return subunit for id
     * @throws InterruptedException       when the client throws such exception.
     * @throws NonExistingUnitError       when the URI does not correspond to an existing unit.
     * @throws HttpClientFailureException when HttpClient receives an error.
     */
    public SubSubUnitDto fetch(URI uri, Language language)
            throws InterruptedException, NonExistingUnitError, HttpClientFailureException {

        HttpRequest httpRequest = createHttpRequest(uri);
        HttpResponse<String> response = sendRequest(httpRequest);
        if (isSuccessful(response.statusCode())) {
            return toUnit(response.body());
        } else {
            throw new NonExistingUnitError(uri.toString());
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest httpRequest) throws InterruptedException,
            HttpClientFailureException {
        try {
            return httpClient.sendAsync(httpRequest, BodyHandlers.ofString()).get();
        } catch (ExecutionException e) {
            throw new HttpClientFailureException(e);
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

    private HttpRequest createHttpRequest(URI uri) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();
    }

}
