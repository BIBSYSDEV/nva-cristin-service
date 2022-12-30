package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.unit.nva.utils.CustomInstantSerializer;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CristinBiobank {
    //Here be more params
    private static final String CRISTIN_BIOBANK_ID = "cristin_biobank_id";
    private static final String CRISTIN_NAME_FIELD = "name";

    private static final String CRISTIN_BIOBANK_COORDINATING_INSTITUTION = "institution";

    private static final String CRISTIN_BIOBANK_LAST_MODIFIED = "last_modified";
    private static final String CRISTIN_BIOBANK_ASSOC_PROJECT = "associated_project";
    private static final String CRISTIN_BIOBANK_TYPE = "type";
    private static final String CRISTIN_BIOBANK_MAIN_LANGUAGE_KEY = "main_language";
    private static final String CRISTIN_BIOBANK_START_DATE = "start_date";
    private static final String CRISTIN_BIOBANK_STORED_UNTIL_DATE = "stored_until_date";
    private static final String CRISTIN_BIOBANK_STATUS = "status";
    private static final String CRISTIN_BIOBANK_CREATED = "created";
    private static final String CRISTIN_BIOBANK_COORDINATOR = "coordinator";
    private static final String CRISTIN_BIOBANK_EXTERNAL_SOURCES = "external_sources";
    private static final String CRISTIN_BIOBANK_APPROVALS = "approvals";
    private static final String CRISTIN_BIOBANK_MATERIALS = "biobank_material";

    @JsonProperty(CRISTIN_BIOBANK_ID)
    private final String cristinBiobankId;

    @JsonProperty(CRISTIN_BIOBANK_TYPE)
    private final String cristinBiobankType;
    @JsonProperty(CRISTIN_NAME_FIELD)
    private final Map<String, String> name;
    @JsonProperty(CRISTIN_BIOBANK_MAIN_LANGUAGE_KEY)
    private final String cristinBiobankLanguage;

    @JsonProperty(CRISTIN_BIOBANK_START_DATE)
    private final Instant cristinBiobankStartDate;

    @JsonProperty(CRISTIN_BIOBANK_STORED_UNTIL_DATE)
    private final Instant cristinBiobankStoreUntilDate;

    @JsonProperty(CRISTIN_BIOBANK_STATUS)
    private final String cristinBiobankStatus;
    @JsonProperty(CRISTIN_BIOBANK_CREATED)
    private final CristinTimeStampFromSource cristinBiobankCreated;
    @JsonProperty(CRISTIN_BIOBANK_LAST_MODIFIED)
    private final CristinTimeStampFromSource cristinBiobankLastModified;

    @JsonProperty(CRISTIN_BIOBANK_COORDINATING_INSTITUTION)
    private final CristinCoordinatinInstitution cristinCoordinatinInstitution;

    @JsonProperty(CRISTIN_BIOBANK_COORDINATOR)
    private final CristinCoordinator cristinBiobankCoordinator;


    @JsonProperty(CRISTIN_BIOBANK_ASSOC_PROJECT)
    private final CristinAssocProjectForBiobank cristinBiobankAssocProject;

    @JsonProperty(CRISTIN_BIOBANK_EXTERNAL_SOURCES)
    private final CristinExternalSourcesBiobank cristinBiobankExternalSources;

    @JsonProperty(CRISTIN_BIOBANK_APPROVALS)
    private final CristinBiobankApprovals cristinBiobankApprovals;


    @JsonProperty(CRISTIN_BIOBANK_MATERIALS)
    private final List<CristinBiobankMaterial> cristinBiobankMaterials;

    @JsonSerialize(using = CustomInstantSerializer.class)
    private final Instant startDate;

    public CristinBiobank(@JsonProperty(CRISTIN_BIOBANK_ID) String cristinBiobankId,
                          @JsonProperty(CRISTIN_BIOBANK_TYPE) String cristinBiobankType,
                          @JsonProperty(CRISTIN_BIOBANK_MAIN_LANGUAGE_KEY) String cristinBiobankLanguage,
                          @JsonProperty(CRISTIN_NAME_FIELD) Map<String, String> name,
                          Instant cristinBiobankStartDate,
                          Instant cristinBiobankStoreUntilDate,
                          String cristinBiobankStatus,
                          CristinTimeStampFromSource cristinBiobankCreated,
                          CristinTimeStampFromSource cristinBiobankLastModified,
                          CristinCoordinatinInstitution cristinCoordinatinInstitution,
                          CristinCoordinator cristinBiobankCoordinator,
                          CristinAssocProjectForBiobank cristinBiobankAssocProject,
                          CristinExternalSourcesBiobank cristinBiobankExternalSources,
                          CristinBiobankApprovals cristinBiobankApprovals,
                          List<CristinBiobankMaterial> cristinBiobankMaterials,
                          Instant startDate) {
        this.cristinBiobankId = cristinBiobankId;
        this.cristinBiobankType = cristinBiobankType;
        this.cristinBiobankLanguage = cristinBiobankLanguage;
        this.name = Collections.unmodifiableMap(name);
        this.cristinBiobankStartDate = cristinBiobankStartDate;
        this.cristinBiobankStoreUntilDate = cristinBiobankStoreUntilDate;
        this.cristinBiobankStatus = cristinBiobankStatus;
        this.cristinBiobankCreated = cristinBiobankCreated;
        this.cristinBiobankLastModified = cristinBiobankLastModified;
        this.cristinCoordinatinInstitution = cristinCoordinatinInstitution;
        this.cristinBiobankCoordinator = cristinBiobankCoordinator;
        this.cristinBiobankAssocProject = cristinBiobankAssocProject;
        this.cristinBiobankExternalSources = cristinBiobankExternalSources;
        this.cristinBiobankApprovals = cristinBiobankApprovals;
        this.cristinBiobankMaterials = cristinBiobankMaterials;
        this.startDate = startDate;
    }


    public String getCristinBiobankId() {
        return cristinBiobankId;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getCristinBiobankType() { return cristinBiobankType; }
    public String getCristinBiobankLanguage() { return cristinBiobankLanguage; }
    public Instant getCristinBiobankStartDate() { return cristinBiobankStartDate; }
    public Instant getCristinBiobankStoreUntilDate() { return cristinBiobankStoreUntilDate; }

    public String getCristinBiobankStatus() { return cristinBiobankStatus; }

    public CristinTimeStampFromSource getCristinBiobankCreated() { return cristinBiobankCreated; }

    public CristinTimeStampFromSource getCristinBiobankLastModified() { return cristinBiobankLastModified; }

    public CristinCoordinator getCristinBiobankCoordinator() { return cristinBiobankCoordinator; }

    public CristinAssocProjectForBiobank getCristinBiobankAssocProject() { return cristinBiobankAssocProject; }

    public CristinExternalSourcesBiobank getCristinBiobankExternalSources() { return cristinBiobankExternalSources; }

    public CristinBiobankApprovals getCristinBiobankApprovals() { return cristinBiobankApprovals; }

    public List<CristinBiobankMaterial> getCristinBiobankMaterials() { return cristinBiobankMaterials; }

    public Instant getStartDate() { return startDate; }


}
