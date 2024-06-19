package no.unit.nva.cristin.projects.model.cristin.adapter;

import static no.unit.nva.cristin.projects.model.nva.Funding.UNCONFIRMED_FUNDING;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.nva.Funding;

public class CristinFundingSourceToFunding implements Function<CristinFundingSource, Funding> {

    public static final String FUNDING_SOURCES = "funding-sources";
    public static final String PATH_SEPARATOR = "/";

    @Override
    public Funding apply(CristinFundingSource cristinFundingSource) {
        return createFunding(cristinFundingSource);
    }

    private Funding createFunding(CristinFundingSource cristinFunding) {
        var source = extractFundingSource(cristinFunding);
        var identifier = cristinFunding.getProjectCode();
        var labels = cristinFunding.getFundingSourceName();

        return new Funding(UNCONFIRMED_FUNDING, source, identifier, labels);
    }

    private URI extractFundingSource(CristinFundingSource cristinFunding) {
        var urlEncodedSourceIdentifier = URLEncoder.encode(cristinFunding.getFundingSourceCode(),
                                                           StandardCharsets.UTF_8);
        var uriString = getNvaApiUri(FUNDING_SOURCES).toString();
        return URI.create(uriString + PATH_SEPARATOR + urlEncodedSourceIdentifier);
    }

}
