package no.unit.nva.cristin.biobank.common;

import java.net.URI;
import no.unit.nva.cristin.funding.sources.model.cristin.CristinBiobank;
import no.unit.nva.cristin.funding.sources.model.nva.Biobank;

public final class MappingUtils {

    private MappingUtils() {
        // no-op
    }

    public static Biobank cristinModelToNvaModel(CristinBiobank cristinFundingSource,
                                                       String domainName,
                                                       String basePath) {
        var id = getBiobankUri(cristinFundingSource.getCode(), domainName, basePath);
        return new Biobank (id, cristinBiobank.getCode(), cristinBiobank.getName());
    }

    private static URI getBiobankUri(String code, String domainName, String basePath) {
        return DomainUriUtils.getBiobankUri(domainName, basePath, code);
    }
}
