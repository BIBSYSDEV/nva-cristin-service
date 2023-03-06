package no.unit.nva.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.biobank.model.cristin.CristinAssocProjectForBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankApprovals;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.biobank.model.cristin.CristinCoordinator;
import no.unit.nva.biobank.model.cristin.CristinTimeStampFromSource;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.nva.ExternalSource;
import no.unit.nva.model.DateInfo;

@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.TooManyFields"})
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Biobank implements JsonSerializable {

    @JsonProperty(CONTEXT)
    private URI context;
    @JsonProperty
    private URI id;
    @JsonProperty
    private String identifier;
    @JsonProperty
    private BiobankType biobankType;
    @JsonProperty
    private Map<String, String> name;
    @JsonProperty
    private String mainLanguage;
    @JsonProperty
    private Instant storeUntilDate;
    @JsonProperty
    private Instant startDate;
    @JsonProperty
    private String status;
    @JsonProperty
    private DateInfo created;
    @JsonProperty
    private DateInfo lastModified;
    @JsonProperty
    private URI coordinatingInstitution;
    @JsonProperty
    private URI coordinatingInstitutionUnit;
    @JsonProperty
    private URI coordinator;
    @JsonProperty
    private URI assocProject;
    @JsonProperty
    private Set<ExternalSource> externalSources;
    @JsonProperty
    private BiobankApprovals approvals;
    @JsonProperty
    private List<BiobankMaterial> biobankMaterials;

    public URI getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public BiobankType getBiobankType() {
        return biobankType;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getMainLanguage() {
        return mainLanguage;
    }

    public Instant getStoreUntilDate() {
        return storeUntilDate;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public String getStatus() {
        return status;
    }

    public DateInfo getCreated() {
        return created;
    }

    public DateInfo getLastModified() {
        return lastModified;
    }

    public URI getCoordinatingInstitution() {
        return coordinatingInstitution;
    }

    public URI getCoordinatingInstitutionUnit() {
        return coordinatingInstitutionUnit;
    }

    public URI getCoordinator() {
        return coordinator;
    }

    public URI getAssocProject() {
        return assocProject;
    }

    public Set<ExternalSource> getExternalSource() {
        return externalSources;
    }

    public BiobankApprovals getApprovals() {
        return approvals;
    }

    public List<BiobankMaterial> getBiobankMaterials() {
        return biobankMaterials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Biobank)) {
            return false;
        }
        Biobank biobank = (Biobank) o;
        return Objects.equals(context, biobank.context)
               && Objects.equals(getId(), biobank.getId())
               && Objects.equals(getIdentifier(), biobank.getIdentifier())
               && Objects.equals(getBiobankType(), biobank.getBiobankType())
               && Objects.equals(getName(), biobank.getName())
               && Objects.equals(getMainLanguage(), biobank.getMainLanguage())
               && Objects.equals(getStoreUntilDate(), biobank.getStoreUntilDate())
               && Objects.equals(getStartDate(), biobank.getStartDate())
               && Objects.equals(getStatus(), biobank.getStatus())
               && Objects.equals(getCreated(), biobank.getCreated())
               && Objects.equals(getLastModified(), biobank.getLastModified())
               && Objects.equals(getCoordinatingInstitution(),
                                 biobank.getCoordinatingInstitution())
               && Objects.equals(getCoordinatingInstitutionUnit(),
                                 biobank.getCoordinatingInstitutionUnit())
               && Objects.equals(getCoordinator(), biobank.getCoordinator())
               && Objects.equals(getAssocProject(), biobank.getAssocProject())
               && Objects.equals(getExternalSource(), biobank.getExternalSource())
               && Objects.equals(getApprovals(), biobank.getApprovals())
               && Objects.equals(getBiobankMaterials(), biobank.getBiobankMaterials());
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, getId(), getIdentifier(), getBiobankType(), getName(), getMainLanguage(),
                            getStoreUntilDate(), getStartDate(), getStatus(), getCreated(), getLastModified(),
                            getCoordinatingInstitution(), getCoordinatingInstitutionUnit(), getCoordinator(),
                            getAssocProject(), getExternalSource(), getApprovals(), getBiobankMaterials());
    }

    public static final class Builder {

        private static final String BIOBANK_PATH = "biobank";
        private static final String INSTITUTIONS_PATH = "institution";
        private static final String PERSONS_PATH = "person";
        private static final String PROJECT_PATH = "project";
        private static final String UNITS_PATH = "organization";
        private final Biobank biobank;

        private Builder() {
            biobank = new Biobank();
        }

        public Builder(CristinBiobank cristinBiobank) {
            this();
            setApprovals(cristinBiobank.getApprovals())
                .setAssocProject(cristinBiobank.getAssocProject())
                .setBiobankMaterials(cristinBiobank.getMaterials())
                .setBiobankType(BiobankType.valueOf(cristinBiobank.getType()))
                .setCoordinatinInstitutionUnit(cristinBiobank.getCoordinatinInstitution())
                .setCoordinatingInstitution(cristinBiobank.getCoordinatinInstitution())
                .setCoordinator(cristinBiobank.getCoordinator())
                .setCreated(cristinBiobank.getCreated())
                .setExternalSources(cristinBiobank.getExternalSources())
                //  .setIdentifier()
                .setId(cristinBiobank.getBiobankId())
                .setLastModified(cristinBiobank.getLastModified())
                .setMainLanguage(cristinBiobank.getLanguage())
                .setName(cristinBiobank.getName())
                .setStartDate(cristinBiobank.getStartDate())
                .setStatus(cristinBiobank.getStatus())
                .setStoreUntilDate(cristinBiobank.getStoreUntilDate());
        }

        public Biobank build() {
            return biobank;
        }

        public Builder setId(String biobankId) {
            biobank.id = getNvaApiId(biobankId, BIOBANK_PATH);
            return this;
        }

        public Builder setIdentifier(String identifier) {
            biobank.identifier = identifier;
            return this;
        }

        public Builder setBiobankType(BiobankType biobankType) {
            biobank.biobankType = biobankType;
            return this;
        }

        public Builder setName(Map<String, String> name) {
            biobank.name = name;
            return this;
        }

        public Builder setMainLanguage(String mainLanguage) {
            biobank.mainLanguage = mainLanguage;
            return this;
        }

        public Builder setStoreUntilDate(Instant storeUntilDate) {
            biobank.storeUntilDate = storeUntilDate;
            return this;
        }

        public Builder setStartDate(Instant startDate) {
            biobank.startDate = startDate;
            return this;
        }

        public Builder setStatus(String status) {
            biobank.status = status;
            return this;
        }

        public Builder setCreated(CristinTimeStampFromSource created) {
            biobank.created =
                Optional.ofNullable(created)
                    .map(date -> new DateInfo(created.getSourceShortName(),created.getDate()))
                    .orElse(null);
            return this;
        }

        public Builder setLastModified(CristinTimeStampFromSource lastModified) {
            biobank.lastModified =
                Optional.ofNullable(lastModified)
                    .map(date -> new DateInfo(lastModified.getSourceShortName(), lastModified.getDate()))
                    .orElse(null);
            return this;
        }

        public Builder setCoordinatingInstitution(CristinOrganization cristinOrganization) {
            biobank.coordinatingInstitution =
                Optional.ofNullable(cristinOrganization)
                    .map(CristinOrganization::getInstitution)
                    .map(CristinInstitution::getCristinInstitutionId)
                    .map(instId -> getNvaApiId(instId, INSTITUTIONS_PATH))
                    .orElse(null);
            return this;
        }

        public Builder setCoordinatinInstitutionUnit(CristinOrganization cristinOrganization) {
            biobank.coordinatingInstitutionUnit =
                Optional.ofNullable(cristinOrganization)
                    .map(CristinOrganization::getInstitutionUnit)
                    .map(CristinUnit::getCristinUnitId)
                    .map(instId -> getNvaApiId(instId, UNITS_PATH))
                    .orElse(null);
            return this;
        }

        public Builder setCoordinator(CristinCoordinator coordinator) {
            biobank.coordinator =
                Optional.ofNullable(coordinator)
                    .map(CristinCoordinator::getCristinPersonIdentifier)
                    .map(instId -> getNvaApiId(instId, PERSONS_PATH))
                    .orElse(null);
            return this;
        }

        public Builder setAssocProject(CristinAssocProjectForBiobank assocProject) {
            biobank.assocProject =
                Optional.ofNullable(assocProject)
                    .map(CristinAssocProjectForBiobank::getCristinProjectIdentificator)
                    .map(instId -> getNvaApiId(instId, PROJECT_PATH))
                    .orElse(null);
            return this;
        }

        public Builder setExternalSources(Set<CristinExternalSource> externalSources) {
            biobank.externalSources =
                externalSources.stream()
                    .map(es -> new ExternalSource(es.getSourceReferenceId(),es.getSourceShortName()))
                    .collect(Collectors.toSet());
            return this;
        }

        public Builder setApprovals(CristinBiobankApprovals approvals) {
            biobank.approvals =
                Optional.ofNullable(approvals)
                    .map(BiobankApprovals::new)
                    .orElse(null);
            return this;
        }

        public Builder setBiobankMaterials(List<CristinBiobankMaterial> materials) {
            biobank.biobankMaterials =
                materials.stream()
                    .map(BiobankMaterial::new)
                    .collect(Collectors.toUnmodifiableList());
            return this;
        }
    }
}
