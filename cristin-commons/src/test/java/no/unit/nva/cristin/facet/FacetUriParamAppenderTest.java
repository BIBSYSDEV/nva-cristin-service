package no.unit.nva.cristin.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.net.URI;
import no.unit.nva.cristin.model.query.CristinFacet;
import no.unit.nva.cristin.model.query.CristinInstitutionFacet;
import no.unit.nva.cristin.model.query.CristinSectorFacet;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

public class FacetUriParamAppenderTest {

    private static final URI idUriWithoutFacet =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor");

    private static final URI idUriWithSingleFacet =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor&sector_facet=UC");

    private static final URI idUriWithMultipleFacet =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor&sector_facet=INSTITUTE,UC");

    private static final URI idUriWithMultipleDifferentFacets =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor&organization_facet=uio&sector_facet=UC");

    @Test
    void shouldDoNothingOnNullValueUri() {
        var actual = new FacetUriParamAppender(null, null)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertNull(actual);
    }

    @Test
    void shouldDoNothingOnNullValue() {
        var actual = new FacetUriParamAppender(idUriWithoutFacet, null)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithoutFacet, actual);
    }

    @Test
    void shouldAppendSingleFacetValueToIdUri() {
        CristinFacet cristinFacet = new CristinSectorFacet("UC", null);

        var actual = new FacetUriParamAppender(idUriWithoutFacet, cristinFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithSingleFacet, actual);
    }

    @Test
    void shouldAppendAnotherFacetValueToIdUriWhenAlreadyHasValueForThatFacet() {
        CristinFacet cristinFacet = new CristinSectorFacet("INSTITUTE", null);

        var actual = new FacetUriParamAppender(idUriWithSingleFacet, cristinFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithMultipleFacet, actual);
    }

    @Test
    void shouldAppendMultipleFacetsToIdUri() {
        CristinFacet institutionFacet = new CristinInstitutionFacet("uio", null);
        CristinFacet sectorFacet = new CristinSectorFacet("UC", null);

        var actual = new FacetUriParamAppender(idUriWithoutFacet, institutionFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        actual = new FacetUriParamAppender(actual, sectorFacet)
                     .create()
                     .getUriWithFacetKeys()
                     .map(UriWrapper::getUri)
                     .orElse(null);

        assertEquals(idUriWithMultipleDifferentFacets, actual);
    }

    @Test
    void shouldNotDuplicateValuesWhenAppendingAnotherFacetValueToIdUri() {
        CristinFacet cristinFacet = new CristinSectorFacet("UC", null);

        var actual = new FacetUriParamAppender(idUriWithSingleFacet, cristinFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithSingleFacet, actual);
    }

}