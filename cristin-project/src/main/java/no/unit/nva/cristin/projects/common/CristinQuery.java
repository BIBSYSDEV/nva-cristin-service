package no.unit.nva.cristin.projects.common;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.EQUAL_OPERATOR;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_URL;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import java.util.Arrays;
import no.unit.nva.cristin.model.QueryParameterKey;
import static no.unit.nva.cristin.model.QueryParameterKey.BIOBANK;
import static no.unit.nva.cristin.model.QueryParameterKey.IGNORE_PATH_PARAMETER_INDEX;
import static no.unit.nva.cristin.model.QueryParameterKey.KEYWORD;
import static no.unit.nva.cristin.model.QueryParameterKey.PARTICIPANT;
import static no.unit.nva.cristin.model.QueryParameterKey.PATH_PROJECT;
import static no.unit.nva.cristin.model.QueryParameterKey.ORGANIZATION;
import static no.unit.nva.cristin.model.QueryParameterKey.STATUS;
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
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings({"Unused", "LooseCoupling"})
public class CristinQuery {

    protected final transient Map<QueryParameterKey, String> pathParameters;
    protected final transient Map<QueryParameterKey, String> queryParameters;
    protected final transient Set<QueryParameterKey> otherRequiredKeys;
    protected transient boolean isNvaQuery;

    protected CristinQuery() {
        queryParameters = new ConcurrentHashMap<>();
        pathParameters = new ConcurrentHashMap<>();
        otherRequiredKeys = new HashSet<>();
    }

    public static CristinQueryBuilder builder() {
        return new CristinQueryBuilder();
    }

    /**
     * Creates a URI to Cristin project with specific ID and language.
     *
     * @param id       Project ID to lookup in Cristin
     * @return an URI to Cristin Projects with ID and language parameters
     */
    public static URI fromIdentifier(String id) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                   .addChild(PROJECTS_PATH)
                   .addChild(id)
                   .getUri();
    }

    public static String getUnitIdFromOrganization(String organizationId) {
        var unitId = attempt(() -> URI.create(organizationId)).or(() -> null).get();
        return extractLastPathElement(unitId);
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
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to NVA (default) Projects with parameters.
     */
    public URI toURI() {
        var children = containsKey(PATH_PROJECT)
            ? new String[]{PATH_PROJECT.getKey(), getValue(PATH_PROJECT)}
            : new String[]{PATH_PROJECT.getKey()};

        return UriWrapper.fromUri(CRISTIN_API_URL)
                   .addChild(children)
                   .addQueryParameters(toParameters())
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
                .filter(this::nvaParameterFilter)
                .collect(Collectors.toMap(this::toNvaQueryName, this::toNvaQueryValue));
        return new TreeMap<>(results);
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
                .filter(f -> f.getKey() != PATH_PROJECT)
                .collect(Collectors.toMap(this::toCristinQueryName, this::toCristinQueryValue));
        return new TreeMap<>(results);
    }

    /**
     * Get value from Query Parameter Map with key.
     *
     * @param key to look up.
     * @return String content raw
     */
    public String getValue(QueryParameterKey key) {
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
    public void setValue(QueryParameterKey key, String value) {
        if (nonNull(value)) {
            queryParameters.put(key, key.isEncode() ? decodeUTF(value) : value);
        }
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @param key   to add to.
     * @param value to assign
     */
    public void setPath(QueryParameterKey key, String value) {
        var nonNullValue = nonNull(value) ? value : EMPTY_STRING;
        pathParameters.put(key, key.isEncode() ? decodeUTF(nonNullValue) : nonNullValue);
    }

    /**
     * Compares content of queryParameters in CristinQuery.
     *
     * @param other CristinQuery to compare
     * @return true if content of Maps are equal
     */
    public boolean areEqual(CristinQuery other) {
        if (queryParameters.size() != other.queryParameters.size()
            || pathParameters.size() != other.pathParameters.size()) {
            return false;
        }

        return
            queryParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())))
            &&
            pathParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())));
    }

    /**
     * Query Parameter map contain key.
     *
     * @param key to check
     * @return true if map contains key.
     */
    public boolean containsKey(QueryParameterKey key) {
        return queryParameters.containsKey(key) || pathParameters.containsKey(key);
    }

    private String[] getNvaPathAsArray() {
        final var pathSize = this.pathParameters.size();
        return
            this.pathParameters
                .entrySet()
                .stream()
                .sorted(byOrdinalDesc())
                .flatMap(entry -> Stream.of(getString(pathSize, entry), entry.getValue()))
                .toArray(String[]::new);
    }

    private String getString(int pathSize, Entry<QueryParameterKey, String> entry) {
        var isProjects = entry.getKey().equals(PATH_PROJECT) && pathSize > 1;
        return isProjects ? entry.getKey().getKey() : entry.getKey().getNvaKey();
    }

    private Comparator<Entry<QueryParameterKey, String>> byOrdinalDesc() {
        return Comparator.comparingInt(k -> k.getKey().ordinal());
    }

    private String toCristinQueryName(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().getKey();
    }

    private String toNvaQueryName(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().getNvaKey();
    }

    private String toNvaQueryValue(Map.Entry<QueryParameterKey, String> entry) {
        var value = entry.getValue();
        return entry.getKey().isEncode()
                   ? encodeUTF(value)
                   : value;
    }

    private String toCristinQueryValue(Map.Entry<QueryParameterKey, String> entry) {
        if (entry.getKey().equals(BIOBANK) || entry.getKey().equals(KEYWORD) || entry.getKey().equals(PARTICIPANT)) {
            final var key = entry.getKey().getKey() + EQUAL_OPERATOR;
            return Arrays.stream(entry.getValue().split(","))
                       .collect(Collectors.joining("&" + key));
        }
        var value = entry.getKey().isEncode()
                        ? encodeUTF(entry.getValue())
                        : entry.getValue();

        if (entry.getKey().equals(STATUS)) {
            return ProjectStatus.valueOf(value).getCristinStatus();
        }
        return entry.getKey().equals(ORGANIZATION) && entry.getValue().matches(PATTERN_IS_URL)
                   ? getUnitIdFromOrganization(value)
                   : value;
    }

    private boolean nvaParameterFilter(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    private String decodeUTF(String encoded) {
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

    private String encodeUTF(String unencoded) {
        return URLEncoder.encode(unencoded, StandardCharsets.UTF_8).replace("%20", "+");
    }
}
