package no.unit.nva.cristin.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CristinFacetUriParamAppenderTest {

    private final URI originalUri =
        URI.create("https://api.cristin-test.uio.no/v2/persons/facets?name=aud");

    private final URI uriWithMultipleFacetValues =
        URI.create("https://api.cristin-test.uio.no/v2/persons/facets?name=aud&institution=ntnu&institution=uio"
                   + "&sector=UC");
    private final URI uriWithSingleFacetValue =
        URI.create("https://api.cristin-test.uio.no/v2/persons/facets?name=aud&institution=uio&sector=UC");

    private Map<String, String> params;

    @BeforeEach
    void setup() {
        params = new HashMap<>();
    }

    @Test
    void shouldDoNothingWhenNoParamsSent() {
        var actual = new CristinFacetUriParamAppender(originalUri, null)
                         .getAppendedUri()
                         .getUri();

        assertEquals(originalUri, actual);
    }

    @Test
    void shouldReturnOriginalUriWhenParamMapDoesNotContainFacetParams() {
        params.put("notAFacet", "hello");

        var actual = new CristinFacetUriParamAppender(originalUri, params)
                         .getAppendedUri()
                         .getUri();

        assertEquals(originalUri, actual);
    }

    @Test
    void shouldAppendParamsOfSingleValueToSingleValueOutputParam() {
        params.put("organizationFacet", "uio");
        params.put("sectorFacet", "UC");

        var actual = new CristinFacetUriParamAppender(originalUri, params)
                         .getAppendedUri()
                         .getUri();

        assertEquals(uriWithSingleFacetValue, actual);
    }

    @Test
    void shouldAppendCommaSeparatedValuesAsMultipleParamsOfEqualKey() {
        params.put("organizationFacet", "ntnu,uio");
        params.put("sectorFacet", "UC");

        var actual = new CristinFacetUriParamAppender(originalUri, params)
                         .getAppendedUri()
                         .getUri();

        assertEquals(uriWithMultipleFacetValues, actual);
    }

}