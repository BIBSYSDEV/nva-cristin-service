package no.unit.nva.common;

import java.net.URI;
import no.unit.nva.cristin.biobank.model.nva.Biobank;
import no.unit.nva.model.cristin.CristinBiobank;

import static no.unit.nva.common.DomainUriUtils.getBiobankParamUri;

public final class MappingUtils {
    private static final String INSTITUTIONS_PATH_ELEMENT = "institutions";
    private static final String UNITS_PATH_ELEMENT = "units";
    private static final String PERSONS_PATH_ELEMENT = "persons";
    private MappingUtils() {
        // no-op
    }



    public static Biobank cristinModelToNvaModel(CristinBiobank cristinBiobank,
                                                 String domainName,
                                                 String basePath
                                                 ) {
        var id = getBiobankUri(cristinBiobank.getCristinBiobankId(), domainName, basePath);

        var institutionId = getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCristinCoordinatinInstitution().getCristinInstitution().getCristinInstitutionId(),
                INSTITUTIONS_PATH_ELEMENT);

        var unitId = getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCristinCoordinatinInstitution().getCristinUnit().getCristinUnitId(),
                UNITS_PATH_ELEMENT);

        var personId = getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCristinBiobankCoordinator().getCristinPersonIdentifier(),
                PERSONS_PATH_ELEMENT);

        return new Biobank (id, cristinBiobank.getCristinBiobankId(),
                cristinBiobank.getCristinBiobankType(), cristinBiobank.getName(),
                cristinBiobank.getCristinBiobankLanguage(), cristinBiobank.getStartDate(),
                cristinBiobank.getCristinBiobankStoreUntilDate(), cristinBiobank.getCristinBiobankStatus(),
                cristinBiobank.getCristinBiobankCreated(), cristinBiobank.getCristinBiobankLastModified(),
                institutionId,
                unitId,
                personId,
                cristinBiobank.getCristinBiobankAssocProject().getCristinProjectId(),
                cristinBiobank.getCristinBiobankExternalSources(),
                cristinBiobank.getCristinBiobankApprovals(),
                cristinBiobank.getCristinBiobankMaterials()
                );
    }

    private static URI getBiobankUri(String cristinBiobankId, String domainName, String basePath) {
        return DomainUriUtils.getBiobankUri(domainName, basePath, cristinBiobankId);
    }
}
