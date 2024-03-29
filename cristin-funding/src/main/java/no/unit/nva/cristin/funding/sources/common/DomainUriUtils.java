package no.unit.nva.cristin.funding.sources.common;

import static nva.commons.core.paths.UriWrapper.HTTPS;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import nva.commons.core.StringUtils;
import nva.commons.core.paths.UriWrapper;

public final class DomainUriUtils {

    private static final String FUNDING_SOURCES_PATH_ELEMENT = "funding-sources";

    private DomainUriUtils() {
        // no-op
    }

    public static URI getFundingSourcesUri(String domainName, String basePath) {
        return getBaseUriWrapper(domainName, basePath).addChild(FUNDING_SOURCES_PATH_ELEMENT).getUri();
    }

    public static URI getFundingSourceUri(String domainName, String basePath, String identifier) {
        var urlEncodedIdentifier = URLEncoder.encode(identifier, StandardCharsets.UTF_8);
        var uriString = getBaseUriWrapper(domainName, basePath)
                   .addChild(FUNDING_SOURCES_PATH_ELEMENT)
                   .toString();
        return URI.create(uriString + "/" + urlEncodedIdentifier);
    }

    private static UriWrapper getBaseUriWrapper(String domainName, String basePath) {
        var wrapper = new UriWrapper(HTTPS, domainName);

        if (StringUtils.isNotEmpty(basePath)) {
            return wrapper.addChild(basePath);
        }
        return wrapper;
    }
}
