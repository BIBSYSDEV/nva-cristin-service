package no.unit.nva.biobank.model.cristin.adapter;

import static no.unit.nva.cristin.model.Constants.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.model.Constants.FHI_BIOBANK_REGISTRY;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.TYPE;
import static no.unit.nva.cristin.model.Constants.VALUE;
import static no.unit.nva.cristin.model.JsonPropertyNames.BIOBANK_ID;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.biobank.model.nva.AssociatedProject;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.biobank.model.nva.BiobankApproval;
import no.unit.nva.biobank.model.nva.BiobankType;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.model.DateInfo;
import no.unit.nva.model.ExternalSource;
import no.unit.nva.model.TypedLabel;

public class CristinBiobankToBiobank implements Function<CristinBiobank, Biobank> {

    @Override
    public Biobank apply(CristinBiobank cristinBiobank) {
        return new Biobank(
            getNvaApiId(cristinBiobank.cristinBiobankId(), BIOBANK_ID),
            toCristinIdentifier(cristinBiobank),
            BiobankType.valueOf(cristinBiobank.type()),
            cristinBiobank.name(),
            cristinBiobank.mainLanguage(),
            cristinBiobank.storeUntilDate(),
            cristinBiobank.startDate(),
            cristinBiobank.status(),
            toDateInfoOrNull(cristinBiobank.created()),
            toDateInfoOrNull(cristinBiobank.lastModified()),
            toCoordinatingUnit(cristinBiobank.coordinatingInstitution()),
            getNvaApiId(cristinBiobank.coordinator().cristinPersonId(), PERSON_PATH_NVA),
            toProjectOrNull(cristinBiobank),
            toExternalSources(cristinBiobank.externalSources()),
            toApprovals(cristinBiobank.approvals()),
            toBiobankMaterials(cristinBiobank.biobankMaterials())
        );
    }

    private List<Map<String, String>> toCristinIdentifier(CristinBiobank cristinBiobank) {

        var cristinBiobankIdOrNull = Optional.ofNullable(cristinBiobank.cristinBiobankId())
                                         .map(this::mapOfCristinBiobankId)
                                         .orElse(null);

        var biobankIdOrNull = Optional.ofNullable(cristinBiobank.biobankId())
                                  .map(this::mapOfFhiBiobankId)
                                  .orElse(null);

        return Stream.of(cristinBiobankIdOrNull, biobankIdOrNull)
                   .filter(Objects::nonNull)
                   .toList();
    }

    private Map<String, String> mapOfCristinBiobankId(String biobankId) {
        return Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE,
                      VALUE, biobankId);
    }

    private Map<String, String> mapOfFhiBiobankId(String biobankId) {
        return Map.of(TYPE, FHI_BIOBANK_REGISTRY,
                      VALUE, biobankId);
    }

    private URI toCoordinatingUnit(CristinOrganization cristinOrganization) {
        return Optional.ofNullable(cristinOrganization)
                   .map(CristinOrganization::getInstitutionUnit)
                   .map(CristinUnit::getCristinUnitId)
                   .map(unitId -> getNvaApiId(unitId, ORGANIZATION_PATH))
                   .orElse(null);
    }

    private Set<ExternalSource> toExternalSources(Set<CristinExternalSource> externalSources) {
        return externalSources.stream()
                   .map(CristinExternalSource::toExternalSource)
                   .collect(Collectors.toUnmodifiableSet());
    }

    private List<BiobankApproval> toApprovals(List<CristinApproval> approvals) {
        return approvals.stream()
                   .map(BiobankApproval::new)
                   .toList();
    }

    private List<TypedLabel> toBiobankMaterials(List<CristinBiobankMaterial> materials) {
        return materials.stream()
                   .map(item -> new TypedLabel(item.materialCode(), item.materialName()))
                   .toList();
    }

    private AssociatedProject toProjectOrNull(CristinBiobank cristinBiobank) {
        return Optional.ofNullable(cristinBiobank.associatedProject())
                   .map(AssociatedProject::new)
                   .orElse(null);
    }

    private DateInfo toDateInfoOrNull(CristinDateInfo cristinDateInfo) {
        return Optional.ofNullable(cristinDateInfo)
                   .map(CristinDateInfo::toDateInfo)
                   .orElse(null);
    }

}
