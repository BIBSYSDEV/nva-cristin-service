package no.unit.nva.cristin.person.client;

import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.getCristinUri;

@JacocoGenerated
public class CristinPersonQuery {

    private static final String CRISTIN_QUERY_PARAMETER_NAME_KEY = "name";
    private static final String CRISTIN_QUERY_PARAMETER_ORGANIZATION_KEY = "institution";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_KEY = "page";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_DEFAULT_VALUE = "1";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY = "per_page";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
    public static final String NIN_PARAM_KEY = "national_id";
    public static final String PARENT_UNIT_ID = "parent_unit_id";


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
        return addLanguage(getCristinUri(identifier, PERSON_PATH));
    }

    /**
     * Creates a URI to Cristin person with ORCID identifier.
     *
     * @param orcid Cristin person ORCID identifier to lookup in Cristin
     * @return an URI to Cristin person with ORCID identifier
     */
    public static URI fromOrcid(String orcid) {
        String identifier = String.format("ORCID:%s", orcid);

        URI uri = new UriWrapper(CRISTIN_API_URL)
            .addChild(PERSON_PATH)
            .addChild(identifier)
            .getUri();

        return addLanguage(uri);
    }

    /**
     * Creates a URI to Cristin person with National Identification Number.
     *
     * @param identifier Cristin person National Identification Number to lookup in Cristin
     * @return an URI to Cristin person with National Identification Number identifier
     */
    public static URI fromNationalIdentityNumber(String identifier) {
        URI uri = new UriWrapper(CRISTIN_API_URL)
            .addChild(PERSON_PATH)
            .addQueryParameter(NIN_PARAM_KEY, identifier)
            .getUri();

        return addLanguage(uri);
    }

    public CristinPersonQuery withName(String name) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_NAME_KEY, name);
        return this;
    }

    public CristinPersonQuery withOrganization(String organization) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_ORGANIZATION_KEY, organization);
        return this;
    }


    public CristinPersonQuery withParentUnitId(String unitId) {
        cristinQueryParameters.put(PARENT_UNIT_ID, unitId);
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
        URI uri = new UriWrapper(CRISTIN_API_URL)
            .addChild(PERSON_PATH)
            .addQueryParameters(cristinQueryParameters)
            .getUri();

        return addLanguage(uri);
    }
}
