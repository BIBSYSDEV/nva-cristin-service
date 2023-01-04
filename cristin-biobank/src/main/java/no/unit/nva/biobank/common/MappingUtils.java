package no.unit.nva.biobank.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.biobank.model.nva.BiobankApprovals;
import no.unit.nva.biobank.model.nva.BiobankMaterial;
import no.unit.nva.biobank.model.nva.ExternalSourcesBiobank;
import no.unit.nva.biobank.model.nva.TimeStampFromSource;
import no.unit.nva.cristin.biobank.model.nva.Biobank;

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

        var institutionId = DomainUriUtils.getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCristinCoordinatinInstitution().getCristinInstitution().getCristinInstitutionId(),
                INSTITUTIONS_PATH_ELEMENT);

        var unitId = DomainUriUtils.getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCristinCoordinatinInstitution().getCristinUnit().getCristinUnitId(),
                UNITS_PATH_ELEMENT);

        var personId = DomainUriUtils.getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCristinBiobankCoordinator().getCristinPersonIdentifier(),
                PERSONS_PATH_ELEMENT);

        return new Biobank (id, cristinBiobank.getCristinBiobankId(),
                cristinBiobank.getCristinBiobankType(), cristinBiobank.getName(),
                cristinBiobank.getCristinBiobankLanguage(), cristinBiobank.getStartDate(),
                cristinBiobank.getCristinBiobankStoreUntilDate(), cristinBiobank.getCristinBiobankStatus(),
                new TimeStampFromSource(cristinBiobank.getCristinBiobankCreated()),
                new TimeStampFromSource(cristinBiobank.getCristinBiobankLastModified()),
                institutionId,
                unitId,
                personId,
                cristinBiobank.getCristinBiobankAssocProject().getCristinProjectId(),
                new ExternalSourcesBiobank(cristinBiobank.getCristinBiobankExternalSources()),
                new BiobankApprovals(cristinBiobank.getCristinBiobankApprovals()),
                getBiobankMaterialList(cristinBiobank.getCristinBiobankMaterials())
                );
    }

    private static URI getBiobankUri(String cristinBiobankId, String domainName, String basePath) {
        return DomainUriUtils.getBiobankUri(domainName, basePath, cristinBiobankId);
    }

    private static List<BiobankMaterial> getBiobankMaterialList (List <CristinBiobankMaterial> cristinListOfMaterials) {
        List nvaListMaterials = new ArrayList<>();
        cristinListOfMaterials.forEach(material -> nvaListMaterials.add(new BiobankMaterial(material)));
        return nvaListMaterials;
    }
}
