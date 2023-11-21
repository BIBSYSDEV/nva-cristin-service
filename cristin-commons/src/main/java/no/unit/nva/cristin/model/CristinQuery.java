package no.unit.nva.cristin.model;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.FACETS_PATH;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.cristin.facet.CristinFacetUriParamAppender;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"Unused", "LooseCoupling"})
public abstract class CristinQuery<T extends Enum<T> & IParameterKey> {

    protected static final Logger logger = LoggerFactory.getLogger(CristinQuery.class);
    protected final transient Map<T, String> pathParameters;
    protected final transient Map<T, String> queryParameters;
    protected final transient Map<T, String> facetParameters;
    protected final transient Set<T> otherRequiredKeys;

    protected CristinQuery() {
        queryParameters = new ConcurrentHashMap<>();
        pathParameters = new ConcurrentHashMap<>();
        facetParameters = new ConcurrentHashMap<>();
        otherRequiredKeys = new HashSet<>();
    }


    public static String getUnitIdFromOrganization(String organizationId) {
        var unitId = attempt(() -> URI.create(organizationId)).or(() -> null).get();
        return extractLastPathElement(unitId);
    }

    /**
     * Creates a URI to Cristin project with specific ID and language.
     *
     * @param id       Project ID to lookup in Cristin
     * @return an URI to Cristin Projects with ID
     */
    public static URI fromIdentifier(String id) {
        return
            UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(PROJECTS_PATH)
                .addChild(id)
                .getUri();
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to Cristin/NVA Projects with parameters.
     */
    public URI toNvaURI() {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild(BASE_PATH)
            .addChild(getNvaPathAsArray())
            .addQueryParameters(toNvaParameters())
            .getUri();
    }

    /**
     * Builds identifier URI in NVA format with added query and facet parameters.
     */
    public URI toNvaFacetURI() {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
                   .addChild(BASE_PATH)
                   .addChild(getNvaPathAsArray())
                   .addQueryParameters(toNvaParametersWithAddedFacets())
                   .getUri();
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to NVA (default) Projects with parameters.
     */
    public URI toURI() {
        return
            UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(getCristinPath())
                .addQueryParameters(toParameters())
                .getUri();
    }

    /**
     * Builds URI to search Cristin projects based on facets and parameters supplied to the builder methods.
     *
     * @return a URI to NVA (default) Projects with parameters and facets.
     */
    public URI toCristinFacetURI() {
        var baseUri = UriWrapper.fromUri(CRISTIN_API_URL)
                          .addChild(getCristinPath())
                          .addChild(FACETS_PATH)
                          .addQueryParameters(toParameters())
                          .getUri();

        return appendFacetsToUri(toFacetParameters(), baseUri);
    }

    private static URI appendFacetsToUri(Map<String, String> parameters, URI cristinUri) {
        return new CristinFacetUriParamAppender(cristinUri, parameters)
                   .getAppendedUri()
                   .getUri();
    }

    /**
     * NVA Query Parameters with string Keys.
     *
     * @return Map
     */
    public Map<String, String> toNvaParameters() {
        var results =
            queryParameters.entrySet().stream()
                .filter(this::ignoreNvaPathParameters)
                .collect(Collectors.toMap(this::toNvaQueryName, this::toNvaQueryValue));
        return new TreeMap<>(results);
    }

    /**
     * NVA Query and Facet Parameters with string Keys.
     *
     * @return Map
     */
    public Map<String, String> toNvaParametersWithAddedFacets() {
        var results =
            Stream.of(queryParameters.entrySet(), facetParameters.entrySet())
                .flatMap(Collection::stream)
                .filter(this::ignoreNvaPathParameters)
                .collect(Collectors.toMap(this::toNvaQueryName, this::toNvaQueryValue));

        var resultsWithConvertedKeys = new TreeMap<>(results);

        results.keySet().forEach(key -> {
            switch (key) {
                case "category" -> {
                    resultsWithConvertedKeys.put("categoryFacet", results.get(key));
                    resultsWithConvertedKeys.remove(key);
                }
                case "funding_source" -> {
                    resultsWithConvertedKeys.put("fundingSourceFacet", results.get(key));
                    resultsWithConvertedKeys.remove(key);
                }
                case "participant" -> {
                    resultsWithConvertedKeys.put("participantFacet", results.get(key));
                    resultsWithConvertedKeys.remove(key);
                }
                default -> {
                    // do nothing
                }
            }
        });

        return resultsWithConvertedKeys;
    }

    /**
     * Cristin Query Parameters with string Keys.
     *
     * @return Map of String and String
     */
    public Map<String, String> toParameters() {
        var results =
            Stream.of(queryParameters.entrySet(), pathParameters.entrySet())
                .flatMap(Collection::stream)
                .filter(this::ignorePathParameters)
                .collect(Collectors.toMap(this::toCristinQueryName, this::toCristinQueryValue));
        return new TreeMap<>(results);
    }

    /**
     * Cristin Facet Parameters with string Keys.
     *
     * @return Map of String and String
     */
    public Map<String, String> toFacetParameters() {
        var results =
            facetParameters.entrySet()
                .stream()
                .collect(Collectors.toMap(this::toCristinQueryName, this::toCristinQueryValue));

        return new TreeMap<>(results);
    }

    /**
     * Get value from Query Parameter Map with key.
     *
     * @param key to look up.
     * @return String content raw
     */
    public String getValue(T key) {
        return queryParameters.containsKey(key)
                   ? queryParameters.get(key)
                   : pathParameters.get(key);
    }

    /**
     * Add a key value pair to Query Parameter Map.
     *
     * @param key   to add to.
     * @param value to assign
     */
    public void setValue(T key, String value) {
        if (nonNull(value)) {
            queryParameters.put(key, key.encoding() != KeyEncoding.NONE ? decodeUTF(value) : value);
        }
    }

    /**
     * Add a key value pair to Facet Parameter Map.
     *
     * @param key   to add to.
     * @param value to assign
     */
    public void setFacet(T key, String value) {
        if (nonNull(value)) {
            facetParameters.put(key, key.encoding() != KeyEncoding.NONE ? decodeUTF(value) : value);
        }
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @param key   to add to.
     * @param value to assign
     */
    public void setPath(T key, String value) {
        var nonNullValue = nonNull(value) ? value : EMPTY_STRING;
        pathParameters.put(key, key.encoding() == KeyEncoding.DECODE ? decodeUTF(nonNullValue) : nonNullValue);
    }

    /**
     * Compares content of queryParameters in CristinQuery.
     *
     * @param other CristinQuery to compare
     * @return true if content of Maps are equal
     */
    public boolean areEqual(CristinQuery<T> other) {
        if (queryParameters.size() != other.queryParameters.size()
            || pathParameters.size() != other.pathParameters.size()
            || facetParameters.size() != other.facetParameters.size())  {
            return false;
        }

        return
            queryParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())))
            &&
            pathParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())))
            &&
            facetParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())));
    }

    /**
     * Query Parameter map contain key.
     *
     * @param key to check
     * @return true if map contains key
     */
    public boolean containsKey(T key) {
        return queryParameters.containsKey(key) || pathParameters.containsKey(key) || facetParameters.containsKey(key);
    }

    /**
     * Query Parameter map remove value having key.
     *
     * @param key to check
     */
    public void removeValue(T key) {
        if (queryParameters.containsKey(key)) {
            queryParameters.remove(key);
        } else {
            pathParameters.remove(key);
        }
    }

    private String[] getNvaPathAsArray() {
        return
            this.pathParameters
                .entrySet()
                .stream()
                .sorted(byOrdinalDesc())
                .flatMap(entry -> Stream.of(getNvaPathItem(this.pathParameters.size(), entry), entry.getValue()))
                .filter(entry -> !entry.isEmpty())
                .toArray(String[]::new);
    }

    protected Comparator<Entry<T, String>> byOrdinalDesc() {
        return Comparator.comparingInt(k -> k.getKey().ordinal());
    }

    protected String toCristinQueryName(Entry<T, String> entry) {
        return entry.getKey().getKey();
    }

    protected String toNvaQueryName(Entry<T, String> entry) {
        return entry.getKey().getNvaKey();
    }

    protected String toNvaQueryValue(Entry<T, String> entry) {
        var value = entry.getValue();
        return entry.getKey().encoding() == KeyEncoding.ENCODE_DECODE
                   ? encodeUTF(value)
                   : value;
    }

    protected String toCristinQueryValue(Entry<T, String> entry) {
        return entry.getKey().encoding() == KeyEncoding.ENCODE_DECODE
                   ? encodeUTF(entry.getValue())
                   : entry.getValue();
    }

    /**
     * Sample code for getNvaPathItem.
     * <p>Usage:</p>
     * <samp>    var isProjects = entry.getKey().equals(PATH_PROJECT) && pathSize > 1;<br>
     *     return isProjects ? entry.getKey().getKey() : entry.getKey().getNvaKey();<br>
     * </samp>
     */
    protected abstract String getNvaPathItem(int pathSize, Entry<T, String> entry);


    /**
     * Sample code for ignoreNvaPathParameters.
     * <p>Usage:</p>
     * <samp>return entry.getKey().ordinal() > IGNORE_PATH_PARAMETER_INDEX;<br>
     * </samp>
     */
    protected abstract boolean ignoreNvaPathParameters(Entry<T, String> entry);


    /**
     * Sample code for ignorePathParameters.
     * <p>Usage:</p>
     * <samp>return f.getKey() != PATH_PROJECT;<br>
     * </samp>
     */
    protected abstract boolean ignorePathParameters(Entry<T, String> f);


    /**
     * Sample code for getCristinPath.
     * <p>Usage:</p>
     * <samp>var children = containsKey(PATH_PROJECT)<br>
     *     ? new String[]{PATH_PROJECT.getKey(), getValue(PATH_PROJECT)}<br>
     *     : new String[]{PATH_PROJECT.getKey()};<br>
     * return children;<br>
     * </samp>
     */
    protected abstract String[] getCristinPath();


    protected String decodeUTF(String encoded) {
        String decode = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        logger.info("decoded " + decode);
        return decode;
    }

    protected String encodeUTF(String unencoded) {
        return URLEncoder.encode(unencoded, StandardCharsets.UTF_8).replace("%20", "+");
    }
}
