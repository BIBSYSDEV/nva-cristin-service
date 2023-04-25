package no.unit.nva.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.Constants.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.model.Constants.FHI_BIOBANK_REGISTRY;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.TYPE;
import static no.unit.nva.cristin.model.Constants.VALUE;
import static no.unit.nva.cristin.model.JsonPropertyNames.BIOBANK_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.beans.ConstructorProperties;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.model.DateInfo;
import no.unit.nva.model.ExternalSource;
import no.unit.nva.model.TypedLabel;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings({"PMD.ExcessiveParameterList", "PMD.TooManyFields"})
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Biobank implements JsonSerializable {

    @JsonProperty(CONTEXT)
    private final URI context;
    private final URI id;
    @JsonPropertyOrder(alphabetic = true)
    private final List<Map<String, String>> identifiers;
    private final BiobankType biobankType;
    private final Map<String, String> name;
    private final String mainLanguage;
    private final Instant storeUntilDate;
    private final Instant startDate;
    private final String status;
    private final DateInfo created;
    private final DateInfo lastModified;
    private final URI coordinatingOrganization;
    private final URI coordinator;
    private final AssociatedProject project;
    private final Set<ExternalSource> externalSources;
    private final List<BiobankApproval> approvals;
    private final List<TypedLabel> biobankMaterials;


    @ConstructorProperties({"id", "identifiers", "biobankType", "name", "mainLanguage", "storeUntilDate", "startDate",
        "status","created", "lastModified", "coordinatingUnit", "coordinator", "project",
        "externalSources","approvals", "biobankMaterials"})
    public Biobank(URI id, List<Map<String, String>> identifiers, BiobankType biobankType, Map<String, String> name,
                   String mainLanguage, Instant storeUntilDate, Instant startDate, String status, DateInfo created,
                   DateInfo lastModified, URI coordinatingOrganization, URI coordinator,
                   AssociatedProject project, Set<ExternalSource> externalSources, List<BiobankApproval> approvals,
                   List<TypedLabel> biobankMaterials) {
        this.context = URI.create("https://bibsysdev.github.io/src/biobank-context.json");
        this.approvals = approvals;
        this.biobankMaterials = biobankMaterials;
        this.biobankType = biobankType;
        this.coordinatingOrganization = coordinatingOrganization;
        this.coordinator = coordinator;
        this.created = created;
        this.externalSources = externalSources;
        this.id = id;
        this.identifiers = identifiers;
        this.lastModified = lastModified;
        this.mainLanguage = mainLanguage;
        this.name = name;
        this.project = project;
        this.startDate = startDate;
        this.status = status;
        this.storeUntilDate = storeUntilDate;
    }

    public Biobank(CristinBiobank cristinBiobank) {
        this(
            getNvaApiId(cristinBiobank.getCristinBiobankId(), BIOBANK_ID),
            toCristinIdentifier(cristinBiobank),
            BiobankType.valueOf(cristinBiobank.getType()),
            cristinBiobank.getName(),
            cristinBiobank.getMainLanguage(),
            cristinBiobank.getStoreUntilDate(),
            cristinBiobank.getStartDate(),
            cristinBiobank.getStatus(),
            toDateInfoOrNull(cristinBiobank.getCreated()),
            toDateInfoOrNull(cristinBiobank.getLastModified()),
            toCoordinatingUnit(cristinBiobank.getCoordinatingInstitution()),
            getNvaApiId(cristinBiobank.getCoordinator().getCristinPersonId(), PERSON_PATH_NVA),
            toProjectOrNull(cristinBiobank),
            toExternalSources(cristinBiobank.getExternalSources()),
            toApprovals(cristinBiobank.getApprovals()),
            toBiobankMaterials(cristinBiobank.getBiobankMaterials())
        );
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

    public List<TypedLabel> getBiobankMaterials() {
        return biobankMaterials;
    }

    @Override
    @JacocoGenerated
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
               && Objects.equals(getCoordinator(), biobank.getCoordinator())
               && Objects.equals(getProject(), biobank.getProject())
               && Objects.equals(getExternalSources(), biobank.getExternalSources())
               && Objects.equals(getApprovals(), biobank.getApprovals())
               && Objects.equals(getBiobankMaterials(), biobank.getBiobankMaterials());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(context, getId(), getIdentifiers(), getBiobankType(), getName(), getMainLanguage(),
                            getStoreUntilDate(), getStartDate(), getStatus(), getCreated(), getLastModified(),
                            getCoordinatingOrganization(), getCoordinator(), getProject(), getExternalSources(),
                            getApprovals(), getBiobankMaterials()
        );
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }

    private static List<Map<String, String>> toCristinIdentifier(CristinBiobank cristinBiobank) {

        var cristinBiobankIdOrNull = nonNull(cristinBiobank.getCristinBiobankId())
            ? Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinBiobank.getCristinBiobankId())
            : null;

        var biobankIdOrNull = nonNull(cristinBiobank.getBiobankId())
            ? Map.of(TYPE, FHI_BIOBANK_REGISTRY, VALUE, cristinBiobank.getBiobankId())
            : null;

        return Stream.of(cristinBiobankIdOrNull,biobankIdOrNull)
                   .filter(Objects::nonNull)
                   .collect(Collectors.toUnmodifiableList());
    }


    private static URI toCoordinatingUnit(CristinOrganization cristinOrganization) {
        return
            Optional.ofNullable(cristinOrganization)
                .map(CristinOrganization::getInstitutionUnit)
                .map(CristinUnit::getCristinUnitId)
                .map(unitId -> getNvaApiId(unitId, ORGANIZATION_PATH))
                .orElse(null);
    }

    private static Set<ExternalSource> toExternalSources(Set<CristinExternalSource> externalSources) {
        return
            externalSources.stream()
                .map(es -> new ExternalSource(es.getSourceReferenceId(), es.getSourceShortName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static List<BiobankApproval> toApprovals(List<CristinApproval> approvals) {
        return
            approvals.stream()
                .map(BiobankApproval::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<TypedLabel> toBiobankMaterials(List<CristinBiobankMaterial> materials) {
        return
            materials.stream()
                .map(item -> new TypedLabel(item.getMaterialCode(),item.getMaterialName()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static AssociatedProject toProjectOrNull(CristinBiobank cristinBiobank) {
        return nonNull(cristinBiobank.getAssociatedProject())
            ? new AssociatedProject(cristinBiobank.getAssociatedProject()) : null;
    }

    private static DateInfo toDateInfoOrNull(CristinDateInfo cristinBiobank) {
        return nonNull(cristinBiobank) ? cristinBiobank.toDateInfo() : null;
    }

}
