package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.biobank.model.nva.Biobank.Builder;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinApproval;
import no.unit.nva.cristin.projects.model.cristin.CristinDateInfo;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;

@SuppressWarnings({"PMD.ExcessiveParameterList","PMD.TooManyFields"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinBiobank {
    // https://api.cristin-test.uio.no/v2/doc/index.html#GETbiobanks

    @JsonProperty
    private String cristinBiobankId;
    @JsonProperty
    private String type;
    @JsonProperty
    private Map<String, String> name;
    @JsonProperty
    private String mainLanguage;
    @JsonProperty
    private Instant startDate;
    @JsonProperty
    private Instant storeUntilDate;
    @JsonProperty
    private String status;
    @JsonProperty
    private CristinDateInfo created;
    @JsonProperty
    private CristinDateInfo lastModified;
    @JsonProperty
    private CristinOrganization coordinatingInstitution;
    @JsonProperty
    private Map<String, String> statusName;
    @JsonProperty
    private Map<String, String> typeName;
    @JsonProperty
    private CristinPerson coordinator;
    @JsonProperty
    private Set<CristinExternalSource> externalSources;
    @JsonProperty
    private List<CristinApproval> approvals;
    @JsonProperty
    private List<CristinBiobankMaterial> biobankMaterials;
    @JsonProperty
    private String biobankId;
    @JsonProperty
    private CristinAssociatedProject associatedProject;


    public String getCristinBiobankId() {
        return cristinBiobankId;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getMainLanguage() {
        return mainLanguage;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public Instant getStoreUntilDate() {
        return storeUntilDate;
    }

    public String getStatus() {
        return status;
    }

    public CristinDateInfo getCreated() {
        return created;
    }

    public CristinDateInfo getLastModified() {
        return lastModified;
    }

    public CristinOrganization getCoordinatingInstitution() {
        return coordinatingInstitution;
    }

    public Map<String, String> getStatusName() {
        return statusName;
    }

    public Map<String, String> getTypeName() {
        return typeName;
    }

    public CristinPerson getCoordinator() {
        return coordinator;
    }

    public Set<CristinExternalSource> getExternalSources() {
        return externalSources;
    }

    public List<CristinApproval> getApprovals() {
        return approvals;
    }

    public List<CristinBiobankMaterial> getBiobankMaterials() {
        return biobankMaterials;
    }

    public String getBiobankId() {
        return biobankId;
    }

    public CristinAssociatedProject getAssociatedProject() {
        return associatedProject;
    }

    public void setCristinBiobankId(String cristinBiobankId) {
        this.cristinBiobankId = cristinBiobankId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public void setMainLanguage(String mainLanguage) {
        this.mainLanguage = mainLanguage;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public void setStoreUntilDate(Instant storeUntilDate) {
        this.storeUntilDate = storeUntilDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreated(CristinDateInfo created) {
        this.created = created;
    }

    public void setLastModified(CristinDateInfo lastModified) {
        this.lastModified = lastModified;
    }

    public void setCoordinatingInstitution(CristinOrganization coordinatingInstitution) {
        this.coordinatingInstitution = coordinatingInstitution;
    }

    public void setStatusName(Map<String, String> statusName) {
        this.statusName = statusName;
    }

    public void setTypeName(Map<String, String> typeName) {
        this.typeName = typeName;
    }

    public void setCoordinator(CristinPerson coordinator) {
        this.coordinator = coordinator;
    }

    public void setExternalSources(Set<CristinExternalSource> externalSources) {
        this.externalSources = externalSources;
    }

    public void setApprovals(List<CristinApproval> approvals) {
        this.approvals = approvals;
    }

    public void setBiobankMaterials(List<CristinBiobankMaterial> biobankMaterials) {
        this.biobankMaterials = biobankMaterials;
    }

    public void setBiobankId(String biobankId) {
        this.biobankId = biobankId;
    }

    public void setAssociatedProject(CristinAssociatedProject associatedProject) {
        this.associatedProject = associatedProject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinBiobank)) {
            return false;
        }
        CristinBiobank that = (CristinBiobank) o;
        return Objects.equals(getCristinBiobankId(), that.getCristinBiobankId())
               && Objects.equals(getType(), that.getType())
               && Objects.equals(getName(), that.getName())
               && Objects.equals(getMainLanguage(), that.getMainLanguage())
               && Objects.equals(getStartDate(), that.getStartDate())
               && Objects.equals(getStoreUntilDate(), that.getStoreUntilDate())
               && Objects.equals(getStatus(), that.getStatus())
               && Objects.equals(getCreated(), that.getCreated())
               && Objects.equals(getLastModified(), that.getLastModified())
               && Objects.equals(getCoordinatingInstitution(), that.getCoordinatingInstitution())
               && Objects.equals(getStatusName(), that.getStatusName())
               && Objects.equals(getTypeName(), that.getTypeName())
               && Objects.equals(getCoordinator(), that.getCoordinator())
               && Objects.equals(getExternalSources(), that.getExternalSources())
               && Objects.equals(getApprovals(), that.getApprovals())
               && Objects.equals(getBiobankId(), that.getBiobankId())
               && Objects.equals(getAssociatedProject(), that.getAssociatedProject())
               && Objects.equals(getBiobankMaterials(), that.getBiobankMaterials());
    }

    @Override
    public int hashCode() {
        return
            Objects.hash(getCristinBiobankId(), getType(), getName(), getMainLanguage(), getStartDate(),
                         getStoreUntilDate(), getStatus(), getCreated(), getLastModified(),getAssociatedProject(),
                         getStatusName(), getTypeName(), getCoordinatingInstitution(), getCoordinator(),
                         getExternalSources(), getApprovals(), getBiobankMaterials(), getBiobankId());
    }

    public Biobank toBiobank() {
        return new Builder(this).build();
    }
}
