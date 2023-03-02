package no.unit.nva.cristin.projects.common;

import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.*;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.*;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static nva.commons.core.attempt.Try.attempt;

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
     * @param language what language we want some of the result fields to be in
     * @return an URI to Cristin Projects with ID and language parameters
     */
    public static URI fromIdAndLanguage(String id, String language) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
            .addChild(PROJECTS_PATH)
            .addChild(id)
            .addQueryParameters(Map.of(LANGUAGE.getKey(), language))
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
        return
            new UriWrapper(HTTPS, DOMAIN_NAME)
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
        return
            UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(PROJECTS_PATH)
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
        return queryParameters.get(key);
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
        if (nonNull(value)) {
            pathParameters.put(key, key.isEncode() ? decodeUTF(value) : value);
        }
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
        var pathParams =
            this.pathParameters
                .entrySet()
                .stream()
                .flatMap(entry -> Stream.of(entry.getKey().getNvaKey(), entry.getValue()))
                .collect(Collectors.toList());

        if (pathParams.isEmpty()) {
            pathParams.add(PROJECT_PATH_NVA);
        } else if (!pathParams.contains(PROJECT_PATH_NVA)) {
            pathParams.add(PROJECTS_PATH);
        }
        return pathParams.toArray(new String[0]);
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
        var value = entry.getKey().isEncode()
            ? encodeUTF(entry.getValue())
            : entry.getValue();

        if (entry.getKey().equals(STATUS)) {
            return ProjectStatus.valueOf(value).getCristinStatus();
        }
        return entry.getKey().equals(PROJECT_ORGANIZATION) && entry.getValue().matches(PATTERN_IS_URL)
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
