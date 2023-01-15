package no.unit.nva.biobank.common;

import no.unit.nva.biobank.model.cristin.CristinAssocProjectForBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.biobank.model.cristin.CristinCoordinator;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.biobank.model.nva.BiobankApprovals;
import no.unit.nva.biobank.model.nva.BiobankMaterial;
import no.unit.nva.biobank.model.nva.ExternalSourcesBiobank;
import no.unit.nva.biobank.model.nva.TimeStampFromSource;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinUnit;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MappingUtils {
    private static final String INSTITUTIONS_PATH_ELEMENT = "institutions";
    private static final String UNITS_PATH_ELEMENT = "units";
    private static final String PERSONS_PATH_ELEMENT = "persons";

    private MappingUtils() {
        // no-op
    }


    /**
     * The new NVA biobank from Cristin Biobank.
     *
     * @param cristinBiobank - cristin model
     * @param domainName     - env
     * @param basePath       - path
     */
    public static Biobank cristinModelToNvaModel(CristinBiobank cristinBiobank,
                                                 String domainName,
                                                 String basePath
    ) {
        var id = getBiobankUri(cristinBiobank.getBiobankId(), domainName, basePath);

        var coordinationInstitution =
                Optional.ofNullable(cristinBiobank.getCoordinatinInstitution());

        var institutionId = coordinationInstitution
                .map(CristinOrganization::getInstitution)
                .map(CristinInstitution::getUrl)
                .map(e -> DomainUriUtils.getBiobankParamUri(domainName, basePath, e, INSTITUTIONS_PATH_ELEMENT))
                .orElse(null);


        var unitId = coordinationInstitution
                .map(CristinOrganization::getInstitutionUnit)
                .map(CristinUnit::getUrl)
                .map(url -> DomainUriUtils
                        .getBiobankParamUri(domainName, basePath, url, UNITS_PATH_ELEMENT))
                .orElse(null);


        var personId = Optional.ofNullable(cristinBiobank.getCoordinator())
                .map(CristinCoordinator::getCristinPersonIdentifier)
                .map(cristinId -> DomainUriUtils
                        .getBiobankParamUri(domainName, basePath, cristinId, PERSONS_PATH_ELEMENT))
                .orElse(null);

        var projectId = Optional.of(cristinBiobank.getAssocProject())
                .map(CristinAssocProjectForBiobank::getCristinProjectId)
                .orElse(null);

        var created = Optional
                .ofNullable(cristinBiobank.getLastModified())
                .map(TimeStampFromSource::new).orElse(null);
        var lastModified = Optional
                .ofNullable(cristinBiobank.getLastModified())
                .map(TimeStampFromSource::new).orElse(null);
        var externalSource = Optional
                .ofNullable(cristinBiobank.getExternalSources())
                .map(ExternalSourcesBiobank::new).orElse(null);
        var approvals = Optional
                .ofNullable(cristinBiobank.getApprovals())
                .map(BiobankApprovals::new).orElse(null);

        return new Biobank(id, cristinBiobank.getBiobankId(),
                cristinBiobank.getType(),
                cristinBiobank.getName(),
                cristinBiobank.getLanguage(), cristinBiobank.getStartDate(),
                cristinBiobank.getStoreUntilDate(), cristinBiobank.getStatus(),
                created,
                lastModified,
                institutionId,
                unitId,
                personId,
                projectId,
                externalSource,
                approvals,
                getBiobankMaterialList(cristinBiobank.getMaterials())
        );
    }


    private static URI getBiobankUri(String cristinBiobankId, String domainName, String basePath) {
        return DomainUriUtils.getBiobankUri(domainName, basePath, cristinBiobankId);
    }

    private static List<BiobankMaterial> getBiobankMaterialList(List<CristinBiobankMaterial> cristinListOfMaterials) {
        var nvaListMaterials = new ArrayList<BiobankMaterial>();
        cristinListOfMaterials.forEach(material -> nvaListMaterials.add(new BiobankMaterial(material)));
        return nvaListMaterials;
    }

}
