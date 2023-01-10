package no.unit.nva.biobank.common;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.biobank.model.nva.BiobankApprovals;
import no.unit.nva.biobank.model.nva.BiobankBuilder;
import no.unit.nva.biobank.model.nva.BiobankMaterial;
import no.unit.nva.biobank.model.nva.ExternalSourcesBiobank;
import no.unit.nva.biobank.model.nva.TimeStampFromSource;

public final class MappingUtils {
    private static final String INSTITUTIONS_PATH_ELEMENT = "institutions";
    private static final String UNITS_PATH_ELEMENT = "units";
    private static final String PERSONS_PATH_ELEMENT = "persons";

    private MappingUtils() {
        // no-op
    }


    /**
     * The new NVA biobank from Cristin Biobank.
     * @param cristinBiobank - cristin model
     * @param domainName - env
     * @param basePath - path
     * @return
     */
    public static Biobank cristinModelToNvaModel(CristinBiobank cristinBiobank,
                                                 String domainName,
                                                 String basePath
                                                 ) {

        var id = getBiobankUri(cristinBiobank.getBiobankId(), domainName, basePath);

        var institutionId= Optional.ofNullable(cristinBiobank.getCoordinatinInstitution()
                .getInstitution().getUrl())
                .isPresent()
                ? DomainUriUtils.getBiobankParamUri(domainName, basePath,
                cristinBiobank.getCoordinatinInstitution().getInstitution().getUrl(), INSTITUTIONS_PATH_ELEMENT)
                : null;

        var unitId = Optional.ofNullable(cristinBiobank.getCoordinatinInstitution()
                .getInstitutionUnit().getUrl())
                .isPresent() ?
                DomainUriUtils.getBiobankParamUri(domainName, basePath,
                        cristinBiobank.getCoordinatinInstitution().getInstitutionUnit().getUrl(), UNITS_PATH_ELEMENT) :
                null;

        var personId = Optional.ofNullable(cristinBiobank.getCoordinator().getCristinPersonIdentifier())
                .isPresent() ?
                DomainUriUtils.getBiobankParamUri(domainName, basePath,
                        cristinBiobank.getCoordinator().getCristinPersonIdentifier(), PERSONS_PATH_ELEMENT) :
                null;

        var language = Optional.ofNullable(cristinBiobank.getLanguage())
                .isPresent() ? cristinBiobank.getLanguage() : null;

        var type = Optional.ofNullable(cristinBiobank.getType())
                .isPresent() ? cristinBiobank.getType() : null;

        var startDate = Optional.ofNullable(cristinBiobank.getStartDate())
                .isPresent() ? cristinBiobank.getStartDate() : null;

        var storeUntilDate = Optional.ofNullable(cristinBiobank.getStoreUntilDate())
                .isPresent() ? cristinBiobank.getStoreUntilDate() : null;

        var status = Optional.ofNullable(cristinBiobank.getStatus())
                .isPresent() ? cristinBiobank.getLanguage() : null;


        return new BiobankBuilder()
                .setBiobankId(id)
                .setBiobankIdentifier(cristinBiobank.getBiobankId())
                .setType(type)
                .setName(cristinBiobank.getName())
                .setMainLanguage(language)
                .setStartDate(startDate)
                .setStoreUntilDate(storeUntilDate)
                .setStatus(status)
                .setCoordinatinInstitutionOrg(institutionId)
                .setCoordinatinInstitutionUnit(unitId)
                .setBiobankCoordinator(personId)
                .setAssocProject(Optional.ofNullable(cristinBiobank.getAssocProject().getCristinProjectId())
                        .isPresent() ? cristinBiobank.getAssocProject().getCristinProjectId() : null)
                .setExternalSources(Optional.ofNullable(cristinBiobank.getExternalSources())
                        .isPresent() ? new ExternalSourcesBiobank(cristinBiobank.getExternalSources()) : null)
                .setApprovals(Optional.ofNullable(cristinBiobank.getApprovals())
                        .isPresent() ? new BiobankApprovals(cristinBiobank.getApprovals()) : null)
                .setBiobankMaterials(Optional.ofNullable(cristinBiobank.getMaterials())
                        .isPresent() ? getBiobankMaterialList(cristinBiobank.getMaterials()) : null)
                .setCreated(Optional.ofNullable(cristinBiobank.getCreated())
                        .isPresent() ? new TimeStampFromSource(cristinBiobank.getCreated()) : null)
                .setLastModified(Optional.ofNullable(cristinBiobank.getLastModified())
                        .isPresent() ? new TimeStampFromSource(cristinBiobank.getLastModified()) : null)
                .createBiobank();
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
