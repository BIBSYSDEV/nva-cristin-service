package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.ExcessiveParameterList")
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
    private final String biobankId;

    @JsonProperty(CRISTIN_BIOBANK_TYPE)
    private final String type;
    @JsonProperty(CRISTIN_NAME_FIELD)

    private final Map<String, String> name;
    @JsonProperty(CRISTIN_BIOBANK_MAIN_LANGUAGE_KEY)
    private final String language;

    @JsonProperty(CRISTIN_BIOBANK_START_DATE)
    private final Instant startDate;

    @JsonProperty(CRISTIN_BIOBANK_STORED_UNTIL_DATE)
    private final Instant storeUntilDate;

    @JsonProperty(CRISTIN_BIOBANK_STATUS)
    private final String status;
    @JsonProperty(CRISTIN_BIOBANK_CREATED)
    private final CristinTimeStampFromSource created;
    @JsonProperty(CRISTIN_BIOBANK_LAST_MODIFIED)
    private final CristinTimeStampFromSource lastModified;

    @JsonProperty(CRISTIN_BIOBANK_COORDINATING_INSTITUTION)
    private final CristinOrganization coordinatinInstitution;

    @JsonProperty(CRISTIN_BIOBANK_COORDINATOR)
    private final CristinCoordinator coordinator;


    @JsonProperty(CRISTIN_BIOBANK_ASSOC_PROJECT)
    private final CristinAssocProjectForBiobank assocProject;

    @JsonProperty(CRISTIN_BIOBANK_EXTERNAL_SOURCES)
    private final CristinExternalSource externalSources;

    @JsonProperty(CRISTIN_BIOBANK_APPROVALS)
    private final CristinBiobankApprovals approvals;


    @JsonProperty(CRISTIN_BIOBANK_MATERIALS)
    private final List<CristinBiobankMaterial> materials;


    /**
     * Constructor.
     * @param cristinBiobankId - cristin id code
     * @param type - type
     * @param cristinBiobankLanguage - code of the language
     * @param name - names mapped with languages
     * @param startDate - timestamp
     * @param cristinBiobankStoreUntilDate - timestamp
     * @param status -status
     * @param created - - timestamp and source of creation short name code
     * @param cristinBiobankLastModified - timestamp and source of modification short name code
     * @param coordinatinInstitution - Information about coordinating institution organization and unit (uri)
     * @param coordinator - Id of coordinator (person URI)
     * @param assocProject - Id of the project (URI)
     * @param externalSources - source short name and reference id
     * @param approvals - approval parameters
     * @param materials - material code and description
     */
    public CristinBiobank(@JsonProperty(CRISTIN_BIOBANK_ID) String cristinBiobankId,
                          @JsonProperty(CRISTIN_BIOBANK_TYPE) String type,
                          @JsonProperty(CRISTIN_BIOBANK_MAIN_LANGUAGE_KEY) String cristinBiobankLanguage,
                          @JsonProperty(CRISTIN_NAME_FIELD) Map<String, String> name,
                          Instant startDate,
                          Instant cristinBiobankStoreUntilDate,
                          String status,
                          CristinTimeStampFromSource created,
                          CristinTimeStampFromSource cristinBiobankLastModified,
                          CristinOrganization coordinatinInstitution,
                          CristinCoordinator coordinator,
                          CristinAssocProjectForBiobank assocProject,
                          CristinExternalSource externalSources,
                          CristinBiobankApprovals approvals,
                          List<CristinBiobankMaterial> materials) {
        this.biobankId = cristinBiobankId;
        this.type = type;
        this.language = cristinBiobankLanguage;
        this.name = Collections.unmodifiableMap(name);
        this.startDate = startDate;
        this.storeUntilDate = cristinBiobankStoreUntilDate;
        this.status = status;
        this.created = created;
        this.lastModified = cristinBiobankLastModified;
        this.coordinatinInstitution = coordinatinInstitution;
        this.coordinator = coordinator;
        this.assocProject = assocProject;
        this.externalSources = externalSources;
        this.approvals = approvals;
        this.materials = materials;
    }

    public String getBiobankId() {
        return biobankId;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getLanguage() {
        return language;
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

    public CristinTimeStampFromSource getCreated() {
        return created;
    }

    public CristinTimeStampFromSource getLastModified() {
        return lastModified;
    }

    public CristinOrganization getCoordinatinInstitution() {
        return coordinatinInstitution;
    }

    public CristinCoordinator getCoordinator() {
        return coordinator;
    }

    public CristinAssocProjectForBiobank getAssocProject() {
        return assocProject;
    }

    public CristinExternalSource getExternalSources() {
        return externalSources;
    }

    public CristinBiobankApprovals getApprovals() {
        return approvals;
    }

    public List<CristinBiobankMaterial> getMaterials() {
        return materials;
    }





}
