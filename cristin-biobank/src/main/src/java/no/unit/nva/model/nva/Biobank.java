package no.unit.nva.cristin.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static javax.management.remote.rmi.RMIConnectorServer.CREATED;
import static no.unit.nva.cristin.model.JsonPropertyNames.*;
import static sun.net.www.protocol.file.FileURLConnection.LAST_MODIFIED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.unit.nva.model.cristin.CristinAssocProjectForBiobank;
import no.unit.nva.model.cristin.CristinBiobankApprovals;
import no.unit.nva.model.cristin.CristinBiobankMaterial;
import no.unit.nva.model.cristin.CristinCoordinatinInstitution;
import no.unit.nva.model.cristin.CristinCoordinator;
import no.unit.nva.model.cristin.CristinExternalSourcesBiobank;
import no.unit.nva.model.cristin.CristinTimeStampFromSource;
import no.unit.nva.model.nva.AssocProjectForBiobank;
import no.unit.nva.model.nva.BiobankApprovals;
import no.unit.nva.model.nva.BiobankMaterial;
import no.unit.nva.model.nva.CoordinatinInstitution;
import no.unit.nva.model.nva.Coordinator;
import no.unit.nva.model.nva.ExternalSourcesBiobank;
import no.unit.nva.model.nva.TimeStampFromSource;
import no.unit.nva.utils.CustomInstantSerializer;
import no.unit.nva.utils.DateInfo;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
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

    @JsonProperty(IDENTIFIER)
    private final String cristinBiobankId;

    @JsonProperty(BIOBANK_TYPE)
    private final String type;
    @JsonProperty(NAME)
    private final Map<String, String> name;
    @JsonProperty(MAIN_LANGUAGE_KEY)
    private final String mainLanguage;

    @JsonProperty(START_DATE)
    private final Instant startDate;

    @JsonProperty(STORED_UNTIL_DATE)
    private final Instant storeUntilDate;

    @JsonProperty(STATUS)
    private final String status;
    @JsonProperty(CREATED)
    private final TimeStampFromSource created;
    @JsonProperty(LAST_MODIFIED)
    private final TimeStampFromSource lastModified;

    @JsonProperty(COORDINATING_INSTITUTION)
    private final CoordinatinInstitution coordinatinInstitution;

    @JsonProperty(COORDINATOR)
    private final Coordinator cristinBiobankCoordinator;


    @JsonProperty(ASSOCIATED_PROJECT)
    private final AssocProjectForBiobank assocProject;

    @JsonProperty(EXTERNAL_SOURCES)
    private final ExternalSourcesBiobank externalSources;

    @JsonProperty(APPROVALS)
    private final BiobankApprovals approvals;


    @JsonProperty(BIOBANK_MATERIALS)
    private final List<BiobankMaterial> biobankMaterials;

    @JsonSerialize(using = CustomInstantSerializer.class)
    private final Instant startDate;


}
