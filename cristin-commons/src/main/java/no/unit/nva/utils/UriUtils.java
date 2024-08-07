package no.unit.nva.utils;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.ALL_QUERY_PARAMETER_LANGUAGES;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.QUERY_PARAMETER_LANGUAGE;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import nva.commons.core.paths.UriWrapper;


public class UriUtils {

    public static final String POSITION = "position";
    public static final String PROJECT = "project";
    public static final String PERSON = "person";

    /**
     * Create URI identifying NVA resource from path and identifier.
     *
     * @param identifier of element
     * @param path       section of NVA API
     * @return valid URI for a NVA resource
     */
    public static URI getNvaApiId(String identifier, String path) {
        return new UriWrapper(HTTPS,
                              DOMAIN_NAME).addChild(BASE_PATH)
            .addChild(path)
            .addChild(identifier)
            .getUri();
    }

    /**
     * Create URI identifying a cristin resource from path and identifier.
     *
     * @param identifier cristin identifier of resource
     * @param path       section of cristin api
     * @return valid URI for a cristin resource
     */
    public static URI getCristinUri(String identifier, String path) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
            .addChild(path)
            .addChild(identifier)
            .getUri();
    }

    /**
     * Add all supported Cristin languages as query param for request to Cristin.
     */
    public static URI addLanguage(URI uri) {
        return UriWrapper.fromUri(uri)
            .addQueryParameter(QUERY_PARAMETER_LANGUAGE, ALL_QUERY_PARAMETER_LANGUAGES)
            .getUri();
    }

    public static URI createIdUriFromParams(Map<String, String> requestQueryParams, String type) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(type)
            .addQueryParameters(requestQueryParams).getUri();
    }

    /**
     * Create a valid query URI for cristin.
     *
     * @param cristinRequestQueryParams limitations for request
     * @param path                      section of cristin API
     * @return valid URI for query against cristin
     */
    public static URI createCristinQueryUri(Map<String, String> cristinRequestQueryParams, String path) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
            .addChild(path)
            .addQueryParameters(cristinRequestQueryParams)
            .getUri();
    }

    public static URI getNvaApiUri(String path) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(path).getUri();
    }

    public static URI createNvaProjectId(String identifier) {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild(BASE_PATH).addChild(UriUtils.PROJECT).addChild(identifier).getUri();
    }

    public static String extractLastPathElement(URI uri) {
        return nonNull(uri) ? UriWrapper.fromUri(uri).getLastPathElement() : null;
    }

    public static URI nvaIdentifierToCristinIdentifier(URI nvaUri, String newPath) {
        return getCristinUri(extractLastPathElement(nvaUri), newPath);
    }

    /**
     * Create position code uri where the position code is the code for an employment title in Cristin.
     */
    public static URI createNvaPositionId(String code) {
        URI positionBase = new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(POSITION).getUri();
        return attempt(() -> new URI(positionBase.getScheme(), positionBase.getHost(), positionBase.getPath(), code))
            .orElseThrow();
    }

    /**
     * Convenience method to decode a uri.
     *
     * @param uri string containing a uri.
     * @return uri decoded from string
     */
    public static String decodeUri(String uri) {
        return URLDecoder.decode(uri, StandardCharsets.UTF_8);
    }

}
