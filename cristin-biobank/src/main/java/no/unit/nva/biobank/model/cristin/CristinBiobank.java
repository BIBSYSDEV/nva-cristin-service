package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.biobank.model.cristin.adapter.CristinBiobankToBiobank;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinPerson;
import nva.commons.core.JacocoGenerated;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"PMD.ExcessiveParameterList","PMD.TooManyFields"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinBiobank implements JsonSerializable {

    private final String cristinBiobankId;
    private final String type;
    private final Map<String, String> name;
    private final String mainLanguage;
    private final Instant startDate;
    private final Instant storeUntilDate;
    private final String status;
    private final CristinDateInfo created;
    private final CristinDateInfo lastModified;
    private final CristinOrganization coordinatingInstitution;
    private final Map<String, String> statusName;
    private final Map<String, String> typeName;
    private final CristinPerson coordinator;
    private final Set<CristinExternalSource> externalSources;
    private final List<CristinApproval> approvals;
    private final List<CristinBiobankMaterial> biobankMaterials;
    private final String biobankId;
    private final CristinAssociatedProject associatedProject;

    /**
     * Cristin Biobank documentation.
     * <a href="https://api.cristin-test.uio.no/v2/doc/index.html#GETbiobanks">...</a>
     */
    @ConstructorProperties({"cristinBiobankId", "type", "name", "mainLanguage", "startDate", "storeUntilDate", "status",
        "created", "lastModified", "coordinatingInstitution", "statusName", "typeName", "coordinator",
        "externalSources", "approvals", "biobankMaterials", "biobankId", "associatedProject"})
    public CristinBiobank(String cristinBiobankId, String type, Map<String, String> name, String mainLanguage,
                          Instant startDate, Instant storeUntilDate, String status, CristinDateInfo created,
                          CristinDateInfo lastModified, CristinOrganization coordinatingInstitution,
                          Map<String, String> statusName, Map<String, String> typeName, CristinPerson coordinator,
                          Set<CristinExternalSource> externalSources, List<CristinApproval> approvals,
                          List<CristinBiobankMaterial> biobankMaterials, String biobankId,
                          CristinAssociatedProject associatedProject) {
        this.cristinBiobankId = cristinBiobankId;
        this.type = type;
        this.name = name;
        this.mainLanguage = mainLanguage;
        this.startDate = startDate;
        this.storeUntilDate = storeUntilDate;
        this.status = status;
        this.created = created;
        this.lastModified = lastModified;
        this.coordinatingInstitution = coordinatingInstitution;
        this.statusName = statusName;
        this.typeName = typeName;
        this.coordinator = coordinator;
        this.externalSources = externalSources;
        this.approvals = approvals;
        this.biobankMaterials = biobankMaterials;
        this.biobankId = biobankId;
        this.associatedProject = associatedProject;
    }

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

    public Biobank toBiobank() {
        return new CristinBiobankToBiobank().apply(this);
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinBiobank that)) {
            return false;
        }
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
    @JacocoGenerated
    public int hashCode() {
        return
            Objects.hash(getCristinBiobankId(), getType(), getName(), getMainLanguage(), getStartDate(),
                         getStoreUntilDate(), getStatus(), getCreated(), getLastModified(),getAssociatedProject(),
                         getStatusName(), getTypeName(), getCoordinatingInstitution(), getCoordinator(),
                         getExternalSources(), getApprovals(), getBiobankMaterials(), getBiobankId());
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }
}
