package no.unit.nva.common;

import java.net.URI;
import no.unit.nva.cristin.biobank.model.nva.Biobank;
import no.unit.nva.model.cristin.CristinBiobank;
import no.unit.nva.common.DomainUriUtils;

public final class MappingUtils {

    private MappingUtils() {
        // no-op
    }

    public static Biobank cristinModelToNvaModel(CristinBiobank cristinBiobank,
                                                 String domainName,
                                                 String basePath) {
        var id = getBiobankUri(cristinBiobank.getCristin_biobank_id(), domainName, basePath);
        return new Biobank (id, cristinBiobank.getCristin_biobank_id(), cristinBiobank.getName());
    }

    private static URI getBiobankUri(String cristin_biobank_id, String domainName, String basePath) {
        return DomainUriUtils.getBiobankUri(domainName, basePath, cristin_biobank_id);
    }
}
