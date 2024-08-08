package no.unit.nva.cristin.funding.sources.common;

import java.net.URI;
import no.unit.nva.cristin.funding.sources.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.funding.sources.model.nva.FundingSource;

public final class MappingUtils {

    private MappingUtils() {
        // no-op
    }

    public static FundingSource cristinModelToNvaModel(CristinFundingSource cristinFundingSource,
                                                       String domainName,
                                                       String basePath) {
        var id = getFundingSourceUri(cristinFundingSource.code(), domainName, basePath);
        return new FundingSource(id, cristinFundingSource.code(), cristinFundingSource.name());
    }

    private static URI getFundingSourceUri(String code, String domainName, String basePath) {
        return DomainUriUtils.getFundingSourceUri(domainName, basePath, code);
    }
}
