package no.unit.nva.common;

import java.net.URI;
import no.unit.nva.cristin.biobank.model.nva.Biobank;
import no.unit.nva.model.cristin.CristinBiobank;

public final class MappingUtils {

    private MappingUtils() {
        // no-op
    }

    public static Biobank cristinModelToNvaModel(CristinBiobank cristinBiobank,
                                                 String domainName,
                                                 String basePath) {
        var id = getBiobankUri(cristinBiobank.getCristinBiobankId(), domainName, basePath);
        return new Biobank (id, cristinBiobank.getCristinBiobankId(), cristinBiobank.getName());
    }

    private static URI getBiobankUri(String cristinBiobankId, String domainName, String basePath) {
        return DomainUriUtils.getBiobankUri(domainName, basePath, cristinBiobankId);
    }
}
