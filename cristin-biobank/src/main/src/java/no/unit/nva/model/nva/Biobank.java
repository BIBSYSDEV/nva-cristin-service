package no.unit.nva.cristin.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.unit.nva.model.nva.BiobankApprovals;
import no.unit.nva.model.nva.BiobankMaterial;
import no.unit.nva.model.nva.ExternalSourcesBiobank;
import no.unit.nva.model.nva.TimeStampFromSource;
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

    private static final String ID_BIOBANK ="id";

    @JsonProperty(ID_BIOBANK)
    private final URI biobankId;

    @JsonProperty(IDENTIFIER)
    private final String biobankIdentifier;

    @JsonProperty(BIOBANK_TYPE)
    private final String biobankType;
    @JsonProperty(NAME)
    private final Map<String, String> name;
    @JsonProperty(MAIN_LANGUAGE_KEY)
    private final String mainLanguage;

    @JsonProperty(STORED_UNTIL_DATE)
    private final Instant storeUntilDate;

    @JsonProperty(STATUS)
    private final String status;
    @JsonProperty(CREATED)
    private transient final TimeStampFromSource created;
    @JsonProperty(LAST_MODIFIED)
    private transient final TimeStampFromSource lastModified;

    @JsonProperty(COORDINATING_INSTITUTION_ORGANIZATION)
    private transient final URI coordinatinInstitutionOrg;

    @JsonProperty(COORDINATING_INSTITUTION_UNIT)
    private transient final  URI coordinatinInstitutionUnit;

    @JsonProperty(COORDINATOR)
    private transient final URI biobankCoordinator;


    @JsonProperty(ASSOCIATED_PROJECT)
    private transient final URI assocProject;

    @JsonProperty(EXTERNAL_SOURCES)
    private transient final ExternalSourcesBiobank externalSources;

    @JsonProperty(APPROVALS)
    private transient final BiobankApprovals approvals;


    @JsonProperty(BIOBANK_MATERIALS)
    private final List<BiobankMaterial> biobankMaterials;

    @JsonSerialize(using = CustomInstantSerializer.class)
    private final Instant startDate;


    public Biobank(URI biobank_id, String biobankIdentifier,
                   String type, Map<String, String> name,
                   String mainLanguage, Instant startDate,
                   Instant storeUntilDate, String status,
                   TimeStampFromSource created, TimeStampFromSource lastModified,
                   URI coordinatinInstitutionOrg,
                   URI coordinatinInstitutionUnit, URI biobankCoordinator,
                   URI assocProject, ExternalSourcesBiobank externalSources,
                   BiobankApprovals approvals, List<BiobankMaterial> biobankMaterials) {
        this.biobankId = biobank_id;
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
