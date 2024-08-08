package no.unit.nva.cristin.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.cristin.model.query.CristinFacetKey;
import no.unit.nva.cristin.model.query.CristinInstitutionFacet;
import no.unit.nva.cristin.model.query.CristinCodeFacet;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

class FacetUriParamAppenderTest {

    private static final URI idUriWithoutFacet =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor");

    private static final URI idUriWithSingleFacet =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor&sectorFacet=UC");

    private static final URI idUriWithMultipleFacet =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor&sectorFacet=INSTITUTE%2CUC");

    private static final URI idUriWithMultipleDifferentFacets =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?name=tor&organizationFacet=uio&sectorFacet=UC");

    private static final URI idUriWithMultipleFacetUnSorted =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/?sectorFacet=UC%2CINSTITUTE&name=tor");

    private static final URI idUriWithMultipleFacetAppendedAndSorted =
        URI.create("https://api.dev.nva.aws.unit.no/cristin/person/"
                   + "?name=tor"
                   + "&organizationFacet=uio"
                   + "&sectorFacet=INSTITUTE%2CUC");

    private static final URI idUriWithTitleHavingComma = generateUriWithCommaParamValue();

    @Test
    void shouldDoNothingOnNullValueUri() {
        var actual = new FacetUriParamAppender(null, null, null)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertNull(actual);
    }

    @Test
    void shouldDoNothingOnNullValue() {
        var actual = new FacetUriParamAppender(idUriWithoutFacet, null, null)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithoutFacet, actual);
    }

    @Test
    void shouldAppendSingleFacetValueToIdUri() {
        var cristinFacet = new CristinCodeFacet("UC", null);

        var actual = new FacetUriParamAppender(idUriWithoutFacet, CristinFacetKey.SECTOR.getKey(), cristinFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithSingleFacet, actual);
    }

    @Test
    void shouldAppendAnotherFacetValueToIdUriWhenAlreadyHasValueForThatFacet() {
        var cristinFacet = new CristinCodeFacet("INSTITUTE", null);

        var actual = new FacetUriParamAppender(idUriWithSingleFacet, CristinFacetKey.SECTOR.getKey(), cristinFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithMultipleFacet, actual);
    }

    @Test
    void shouldAppendMultipleFacetsToIdUri() {
        var institutionFacet = new CristinInstitutionFacet("uio", null);
        var sectorFacet = new CristinCodeFacet("UC", null);

        var actual = new FacetUriParamAppender(idUriWithoutFacet,
                                               CristinFacetKey.INSTITUTION.getKey(),
                                               institutionFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        actual = new FacetUriParamAppender(actual, CristinFacetKey.SECTOR.getKey(), sectorFacet)
                     .create()
                     .getUriWithFacetKeys()
                     .map(UriWrapper::getUri)
                     .orElse(null);

        assertEquals(idUriWithMultipleDifferentFacets, actual);
    }

    @Test
    void shouldNotDuplicateValuesWhenAppendingAnotherFacetValueToIdUri() {
        var cristinFacet = new CristinCodeFacet("UC", null);

        var actual = new FacetUriParamAppender(idUriWithSingleFacet, CristinFacetKey.SECTOR.getKey(), cristinFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithSingleFacet, actual);
    }

    @Test
    void shouldSortUriParametersAlphabeticallyWhenHasNewFacets() {
        var institutionFacet = new CristinInstitutionFacet("uio", null);

        var actual = new FacetUriParamAppender(idUriWithMultipleFacetUnSorted,
                                               CristinFacetKey.INSTITUTION.getKey(),
                                               institutionFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        assertEquals(idUriWithMultipleFacetAppendedAndSorted, actual);
    }

    @Test
    void shouldExcludeParamsForSortingOnCommaWhenCommaIsPartOfTheSearchValue() {
        var institutionFacet = new CristinInstitutionFacet("uio", null);

        var actual = new FacetUriParamAppender(idUriWithTitleHavingComma,
                                               CristinFacetKey.PARTICIPATING_PERSON_ORG.getKey(),
                                               institutionFacet)
                         .create()
                         .getUriWithFacetKeys()
                         .map(UriWrapper::getUri)
                         .orElse(null);

        var expected = "https://api.dev.nva.aws.unit.no/cristin/project/?participantOrgFacet=uio"
                       + "&title=Et%20prestisjefylt%2C%20stort%20og%20viktig%20prosjekt";

        Objects.requireNonNull(actual);
        assertEquals(expected, actual.toString());
    }

    private static URI generateUriWithCommaParamValue() {
        return UriWrapper.fromUri("https://api.dev.nva.aws.unit.no/cristin/project/")
                      .addQueryParameter("title", "Et prestisjefylt, stort og viktig prosjekt")
                      .getUri();
    }

}
