package no.unit.nva.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.TYPE;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.VALUE;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.biobank.model.cristin.CristinAssociatedProject;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.cristin.CristinApproval;
import no.unit.nva.cristin.projects.model.cristin.CristinDateInfo;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
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
    @JsonPropertyOrder(alphabetic = true)
    private List<Map<String, String>> identifiers = new ArrayList<>();
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
    private URI coordinatingOrganization;
    @JsonProperty
    private URI coordinatingUnit;
    @JsonProperty
    private URI coordinator;
    @JsonProperty
    private AssociatedProject project;
    @JsonProperty
    private Set<ExternalSource> externalSources;
    @JsonProperty
    private List<BiobankApproval> approvals;
    @JsonProperty
    private List<BiobankMaterial> biobankMaterials;

    public Biobank() {
    }

    public URI getId() {
        return id;
    }

    public URI getContext() {
        return context;
    }

    public List<Map<String, String>> getIdentifiers() {
        return nonEmptyOrDefault(identifiers);
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

    public URI getCoordinatingOrganization() {
        return coordinatingOrganization;
    }

    public URI getCoordinatingUnit() {
        return coordinatingUnit;
    }

    public URI getCoordinator() {
        return coordinator;
    }

    public AssociatedProject getProject() {
        return project;
    }

    public Set<ExternalSource> getExternalSources() {
        return externalSources;
    }

    public List<BiobankApproval> getApprovals() {
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
               && Objects.equals(getIdentifiers(), biobank.getIdentifiers())
               && Objects.equals(getBiobankType(), biobank.getBiobankType())
               && Objects.equals(getName(), biobank.getName())
               && Objects.equals(getMainLanguage(), biobank.getMainLanguage())
               && Objects.equals(getStoreUntilDate(), biobank.getStoreUntilDate())
               && Objects.equals(getStartDate(), biobank.getStartDate())
               && Objects.equals(getStatus(), biobank.getStatus())
               && Objects.equals(getCreated(), biobank.getCreated())
               && Objects.equals(getLastModified(), biobank.getLastModified())
               && Objects.equals(getCoordinatingOrganization(), biobank.getCoordinatingOrganization())
               && Objects.equals(getCoordinatingUnit(), biobank.getCoordinatingUnit())
               && Objects.equals(getCoordinator(), biobank.getCoordinator())
               && Objects.equals(getProject(), biobank.getProject())
               && Objects.equals(getExternalSources(), biobank.getExternalSources())
               && Objects.equals(getApprovals(), biobank.getApprovals())
               && Objects.equals(getBiobankMaterials(), biobank.getBiobankMaterials());
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, getId(), getIdentifiers(), getBiobankType(), getName(), getMainLanguage(),
                            getStoreUntilDate(), getStartDate(), getStatus(), getCreated(), getLastModified(),
                            getCoordinatingOrganization(), getCoordinatingUnit(), getCoordinator(),
                            getProject(), getExternalSources(), getApprovals(), getBiobankMaterials());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Biobank.class.getSimpleName() + "[", "]")
                   .add("context=" + context)
                   .add("id=" + id)
                   .add("identifier=" + identifiers.size())
                   .add("biobankType=" + biobankType)
                   .add("name=" + name)
                   .add("mainLanguage='" + mainLanguage + "'")
                   .add("storeUntilDate=" + storeUntilDate)
                   .add("startDate=" + startDate)
                   .add("status='" + status + "'")
                   .add("created=" + created)
                   .add("lastModified=" + lastModified)
                   .add("coordinatingInstitution=" + coordinatingOrganization)
                   .add("coordinatingInstitutionUnit=" + coordinatingUnit)
                   .add("coordinator=" + coordinator)
                   .add("assocProject=" + project)
                   .add("externalSources=" + externalSources)
                   .add("approvals=" + approvals)
                   .add("biobankMaterials=" + biobankMaterials)
                   .toString();
    }

    public static final class Builder {

        private static final String BIOBANK_PATH = "biobank";
        private static final String PERSONS_PATH = "person";
        private static final String UNITS_PATH = "organization";
        private final Biobank biobank;

        private Builder() {
            biobank = new Biobank();
        }

        public Builder(CristinBiobank cristinBiobank) {
            this();
            biobank.context = URI.create("https://bibsysdev.github.io/src/biobank-context.json");
            setMainLanguage(cristinBiobank.getMainLanguage())
                .setApprovals(cristinBiobank.getApprovals())
                .setBioBankId(cristinBiobank.getBiobankId())
                .setBiobankMaterials(cristinBiobank.getBiobankMaterials())
                .setBiobankType(BiobankType.valueOf(cristinBiobank.getType()))
                .setCoordinatinUnit(cristinBiobank.getCoordinatingInstitution())
                .setCoordinatingOrganization(cristinBiobank.getCoordinatingInstitution())
                .setCoordinator(cristinBiobank.getCoordinator())
                .setCreated(cristinBiobank.getCreated())
                .setCristinBioBankId(cristinBiobank.getCristinBiobankId())
                .setExternalSources(cristinBiobank.getExternalSources())
                .setIdentifiers(createCristinIdentifier(cristinBiobank))
                .setLastModified(cristinBiobank.getLastModified())
                .setName(cristinBiobank.getName())
                .setProject(cristinBiobank.getAssociatedProject())
                .setStartDate(cristinBiobank.getStartDate())
                .setStatus(cristinBiobank.getStatus())
                .setStoreUntilDate(cristinBiobank.getStoreUntilDate())
            ;
        }


        public Biobank build() {
            return biobank;
        }

        public Builder setCristinBioBankId(String cristinBioBankId) {
            if (nonNull(cristinBioBankId)) {
                biobank.id = getNvaApiId(cristinBioBankId, BIOBANK_PATH);
            }
            return this;
        }

        public Builder setProject(CristinAssociatedProject associatedProject) {
            if (nonNull(associatedProject)) {
                biobank.project = new AssociatedProject(associatedProject);
            }
            return this;
        }

        public Builder setIdentifiers(List<Map<String, String>> cristinIdentifier) {
            biobank.identifiers = cristinIdentifier;
            return this;
        }

        private Builder setBioBankId(String biobankId) {
            if (nonNull(biobankId)) {
                biobank.getIdentifiers().add(Map.of("FHI-BiobankRegistry", biobankId));
            }
            return this;
        }

        public Builder setCoordinator(CristinPerson coordinator) {
            biobank.coordinator = getNvaApiId(coordinator.getCristinPersonId(), PERSONS_PATH);
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

        public Builder setCreated(CristinDateInfo created) {
            biobank.created =
                Optional.ofNullable(created)
                    .map(date -> new DateInfo(created.getSourceShortName(), created.getDate()))
                    .orElse(null);
            return this;
        }

        public Builder setLastModified(CristinDateInfo lastModified) {
            biobank.lastModified =
                Optional.ofNullable(lastModified)
                    .map(date -> new DateInfo(lastModified.getSourceShortName(), lastModified.getDate()))
                    .orElse(null);
            return this;
        }

        public Builder setCoordinatingOrganization(CristinOrganization cristinOrganization) {
            biobank.coordinatingOrganization =
                Optional.ofNullable(cristinOrganization)
                    .map(CristinOrganization::getInstitution)
                    .map(CristinInstitution::getCristinInstitutionId)
                    .map(instId -> getNvaApiId(String.format("%s.0.0.0", instId), UNITS_PATH))
                    .orElse(null);
            return this;
        }

        public Builder setCoordinatinUnit(CristinOrganization cristinOrganization) {
            biobank.coordinatingUnit =
                Optional.ofNullable(cristinOrganization)
                    .map(CristinOrganization::getInstitutionUnit)
                    .map(CristinUnit::getCristinUnitId)
                    .map(instId -> getNvaApiId(instId, UNITS_PATH))
                    .orElse(null);
            return this;
        }

        public Builder setExternalSources(Set<CristinExternalSource> externalSources) {
            biobank.externalSources =
                externalSources.stream()
                    .map(es -> new ExternalSource(es.getSourceReferenceId(), es.getSourceShortName()))
                    .collect(Collectors.toSet());
            return this;
        }

        public Builder setApprovals(List<CristinApproval> approvals) {
            biobank.approvals =
                approvals.stream()
                    .map(BiobankApproval::new)
                    .collect(Collectors.toUnmodifiableList());
            return this;
        }

        public Builder setBiobankMaterials(List<CristinBiobankMaterial> materials) {
            biobank.biobankMaterials =
                materials.stream()
                    .map(BiobankMaterial::new)
                    .collect(Collectors.toUnmodifiableList());
            return this;
        }

        private List<Map<String, String>> createCristinIdentifier(CristinBiobank cristinBiobank) {
            var id = nonNull(cristinBiobank.getCristinBiobankId())
                         ? Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinBiobank.getCristinBiobankId())
                         : null;
            var biobank = nonNull(cristinBiobank.getBiobankId())
                         ? Map.of(TYPE, "FHI-BiobankRegistry", VALUE, cristinBiobank.getBiobankId())
                         : null;

            return Stream.of(id,biobank).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
        }
    }
}
