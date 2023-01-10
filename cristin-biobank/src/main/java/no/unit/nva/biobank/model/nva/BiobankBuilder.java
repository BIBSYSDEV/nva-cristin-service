package no.unit.nva.biobank.model.nva;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class BiobankBuilder {
    private URI biobankId;
    private String biobankIdentifier;
    private String type;
    private Map<String, String> name;
    private String mainLanguage;
    private Instant startDate;
    private Instant storeUntilDate;
    private String status;
    private TimeStampFromSource created;
    private TimeStampFromSource lastModified;
    private URI coordinatinInstitutionOrg;
    private URI coordinatinInstitutionUnit;
    private URI biobankCoordinator;
    private URI assocProject;
    private ExternalSourcesBiobank externalSources;
    private BiobankApprovals approvals;
    private List<BiobankMaterial> biobankMaterials;

    public BiobankBuilder setBiobankId(URI biobankId) {
        this.biobankId = biobankId;
        return this;
    }

    public BiobankBuilder setBiobankIdentifier(String biobankIdentifier) {
        this.biobankIdentifier = biobankIdentifier;
        return this;
    }

    public BiobankBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public BiobankBuilder setName(Map<String, String> name) {
        this.name = name;
        return this;
    }

    public BiobankBuilder setMainLanguage(String mainLanguage) {
        this.mainLanguage = mainLanguage;
        return this;
    }

    public BiobankBuilder setStartDate(Instant startDate) {
        this.startDate = startDate;
        return this;
    }

    public BiobankBuilder setStoreUntilDate(Instant storeUntilDate) {
        this.storeUntilDate = storeUntilDate;
        return this;
    }

    public BiobankBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    public BiobankBuilder setCreated(TimeStampFromSource created) {
        this.created = created;
        return this;
    }

    public BiobankBuilder setLastModified(TimeStampFromSource lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public BiobankBuilder setCoordinatinInstitutionOrg(URI coordinatinInstitutionOrg) {
        this.coordinatinInstitutionOrg = coordinatinInstitutionOrg;
        return this;
    }

    public BiobankBuilder setCoordinatinInstitutionUnit(URI coordinatinInstitutionUnit) {
        this.coordinatinInstitutionUnit = coordinatinInstitutionUnit;
        return this;
    }

    public BiobankBuilder setBiobankCoordinator(URI biobankCoordinator) {
        this.biobankCoordinator = biobankCoordinator;
        return this;
    }

    public BiobankBuilder setAssocProject(URI assocProject) {
        this.assocProject = assocProject;
        return this;
    }

    public BiobankBuilder setExternalSources(ExternalSourcesBiobank externalSources) {
        this.externalSources = externalSources;
        return this;
    }

    public BiobankBuilder setApprovals(BiobankApprovals approvals) {
        this.approvals = approvals;
        return this;
    }

    public BiobankBuilder setBiobankMaterials(List<BiobankMaterial> biobankMaterials) {
        this.biobankMaterials = biobankMaterials;
        return this;
    }

    public Biobank createBiobank() {
        return new Biobank(biobankId, biobankIdentifier, type, name, mainLanguage, startDate, storeUntilDate, status, created, lastModified, coordinatinInstitutionOrg, coordinatinInstitutionUnit, biobankCoordinator, assocProject, externalSources, approvals, biobankMaterials);
    }
}