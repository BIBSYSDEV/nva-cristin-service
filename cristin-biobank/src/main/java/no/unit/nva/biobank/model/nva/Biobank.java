package no.unit.nva.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.unit.nva.utils.CustomInstantSerializer;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Biobank {
    private static final String BIOBANK_MATERIALS = "biobankMaterials";
    private static final String APPROVALS = "approvals";
    private static final String EXTERNAL_SOURCES = "externalSources";
    private static final String ASSOCIATED_PROJECT = "associatedProject";
    private static final String COORDINATOR = "coordinator";
    private static final String LAST_MODIFIED = "lastModified";
    private static final String CREATED = "created";
    private static final String STORED_UNTIL_DATE = "storedUntilDate";
    private static final String MAIN_LANGUAGE_KEY = "mainLanguage";
    private static final String BIOBANK_TYPE = "biobankType";
    private static final String COORDINATING_INSTITUTION_ORGANIZATION = "coordinating_institution_org";
    private static final String COORDINATING_INSTITUTION_UNIT = "coordinating_institution_unit";

    private static final String ID_BIOBANK = "id";

    @JsonProperty(ID_BIOBANK)
    private final transient URI biobankId;

    @JsonProperty(IDENTIFIER)
    private final transient String biobankIdentifier;

    @JsonProperty(BIOBANK_TYPE)
    private final transient String biobankType;
    @JsonProperty(NAME)
    private final transient Map<String, String> name;
    @JsonProperty(MAIN_LANGUAGE_KEY)
    private final transient String mainLanguage;

    @JsonProperty(STORED_UNTIL_DATE)
    private final transient Instant storeUntilDate;

    @JsonProperty(STATUS)
    private final  transient String status;
    @JsonProperty(CREATED)
    private final transient TimeStampFromSource created;
    @JsonProperty(LAST_MODIFIED)
    private final transient TimeStampFromSource lastModified;

    @JsonProperty(COORDINATING_INSTITUTION_ORGANIZATION)
    private final transient URI coordinatinInstitutionOrg;

    @JsonProperty(COORDINATING_INSTITUTION_UNIT)
    private final transient  URI coordinatinInstitutionUnit;

    @JsonProperty(COORDINATOR)
    private final transient URI biobankCoordinator;


    @JsonProperty(ASSOCIATED_PROJECT)
    private final transient URI assocProject;

    @JsonProperty(EXTERNAL_SOURCES)
    private final transient ExternalSourcesBiobank externalSources;

    @JsonProperty(APPROVALS)
    private final transient BiobankApprovals approvals;


    @JsonProperty(BIOBANK_MATERIALS)
    private final transient List<BiobankMaterial> biobankMaterials;

    @JsonSerialize(using = CustomInstantSerializer.class)
    private final transient Instant startDate;

    /**
     * Constructor.
     * @param biobankId - biobank URI
     * @param biobankIdentifier - cristin id code
     * @param type - type
     * @param name - names mapped with languages
     * @param mainLanguage - code of the language
     * @param startDate - timestamp
     * @param storeUntilDate - timestamp
     * @param status - status
     * @param created - timestamp and source of creation short name code
     * @param lastModified - timestamp and source of modification short name code
     * @param coordinatinInstitutionOrg - Id of the institution (URI)
     * @param coordinatinInstitutionUnit - Id of the unit (uri)
     * @param biobankCoordinator - Id of coordinator (person URI)
     * @param assocProject - Id of the project (URI)
     * @param externalSources - source short name and reference id
     * @param approvals - approval parameters
     * @param biobankMaterials - material code and description
     */
    public Biobank(URI biobankId, String biobankIdentifier,
                   String type, Map<String, String> name,
                   String mainLanguage, Instant startDate,
                   Instant storeUntilDate, String status,
                   TimeStampFromSource created, TimeStampFromSource lastModified,
                   URI coordinatinInstitutionOrg,
                   URI coordinatinInstitutionUnit, URI biobankCoordinator,
                   URI assocProject, ExternalSourcesBiobank externalSources,
                   BiobankApprovals approvals, List<BiobankMaterial> biobankMaterials) {
        this.biobankId = biobankId;
        this.biobankIdentifier = biobankIdentifier;
        this.biobankType = type;
        this.name = name;
        this.mainLanguage = mainLanguage;
        this.startDate = startDate;
        this.storeUntilDate = storeUntilDate;
        this.status = status;
        this.created = created;
        this.lastModified = lastModified;
        this.coordinatinInstitutionOrg = coordinatinInstitutionOrg;
        this.coordinatinInstitutionUnit = coordinatinInstitutionUnit;
        this.biobankCoordinator = biobankCoordinator;
        this.assocProject = assocProject;
        this.externalSources = externalSources;
        this.approvals = approvals;
        this.biobankMaterials = biobankMaterials;
    }
}
