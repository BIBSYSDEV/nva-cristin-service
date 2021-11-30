package no.unit.nva.cristin.person.client;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_HOST;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

@JacocoGenerated
public class CristinPersonQuery {

    private static final String CRISTIN_QUERY_PARAMETER_NAME_KEY = "name";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_KEY = "page";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_DEFAULT_VALUE = "1";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY = "per_page";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
    private static final String CRISTIN_API_PERSONS_PATH = "/v2/persons/";

    private final transient Map<String, String> cristinQueryParameters;

    /**
     * Creates a object used to generate URI to connect to Cristin Persons.
     */
    public CristinPersonQuery() {
        cristinQueryParameters = new ConcurrentHashMap<>();
        cristinQueryParameters.put(
            CRISTIN_QUERY_PARAMETER_PAGE_KEY,
            CRISTIN_QUERY_PARAMETER_PAGE_DEFAULT_VALUE);
        cristinQueryParameters.put(
            CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY,
            CRISTIN_QUERY_PARAMETER_PER_PAGE_DEFAULT_VALUE);
    }

    /**
     * Creates a URI to Cristin person with specific identifier.
     *
     * @param identifier Cristin person identifier to lookup in Cristin
     * @return an URI to Cristin person with identifier
     */
    public static URI fromId(String identifier) {
        return addLanguage(getCristinUri(identifier, CRISTIN_API_PERSONS_PATH));
    }

    public CristinPersonQuery withName(String name) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_NAME_KEY, name);
        return this;
    }

    public CristinPersonQuery withFromPage(String page) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PAGE_KEY, page);
        return this;
    }

    public CristinPersonQuery withItemsPerPage(String itemsPerPage) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY, itemsPerPage);
        return this;
    }

    /**
     * Builds URI to search Cristin persons based on parameters supplied to the builder methods.
     *
     * @return an URI to Cristin Persons with parameters
     */
    public URI toURI() {
        URI uri = new UriWrapper(HTTPS, CRISTIN_API_HOST)
            .addChild(CRISTIN_API_PERSONS_PATH)
            .addQueryParameters(cristinQueryParameters)
            .getUri();

        return addLanguage(uri);
    }
}
