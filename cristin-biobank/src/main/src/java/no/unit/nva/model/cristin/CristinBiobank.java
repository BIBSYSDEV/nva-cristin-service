package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.unit.nva.utils.CustomInstantSerializer;
import no.unit.nva.utils.DateInfo;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public final class CristinBiobank {
    //Here be more params
    private static final String CRISTIN_BIOBANK_ID = "cristin_biobank_id";
    private static final String CRISTIN_NAME_FIELD = "name";

    private static final String CRISTIN_BIOBANK_INSTITUTION = "institution";

    private static final String CRISTIN_BIOBANK_MODIFIED_SINCE = "modified_since";
    private static final String CRISTIN_BIOBANK_PROJECT_CODE_KEY = "project";
    private static final String CRISTIN_BIOBANK_TYPE = "type";
    private static final String CRISTIN_BIOBANK_LANGUAGE_KEY = "lang";
    private static final String CRISTIN_BIOBANK_PAGE_KEY = "page";
    private static final String CRISTIN_BIOBANK_PER_PAGE_KEY = "per_page";

    @JsonProperty(CRISTIN_BIOBANK_ID)
    private final String cristinBiobankId;

    @JsonProperty(CRISTIN_BIOBANK_INSTITUTION)
    private final String cristinBiobankInstitution;

    @JsonProperty(CRISTIN_BIOBANK_MODIFIED_SINCE)
    private final DateInfo cristinBiobankModifiedSince;
    @JsonProperty(CRISTIN_BIOBANK_PROJECT_CODE_KEY)
    private final String cristinBiobankProjectCodeKey;

    @JsonProperty(CRISTIN_BIOBANK_TYPE)
    private final String cristinBiobankType;
    @JsonProperty(CRISTIN_BIOBANK_LANGUAGE_KEY)
    private final String cristinBiobankLanguage;

    @JsonProperty(CRISTIN_BIOBANK_PAGE_KEY)
    private final String cristinBiobankPageKey;


    @JsonProperty(CRISTIN_BIOBANK_PER_PAGE_KEY)
    private final String cristinBiobankPerPageKey;

    @JsonProperty(CRISTIN_NAME_FIELD)
    private final Map<String, String> name;

    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant startDate;

    public CristinBiobank(@JsonProperty(CRISTIN_BIOBANK_ID) String cristinBiobankId,
                          @JsonProperty(CRISTIN_BIOBANK_INSTITUTION) String cristinBiobankInstitution,
                          @JsonProperty(CRISTIN_BIOBANK_MODIFIED_SINCE) DateInfo cristinBiobankModifiedSince,
                          @JsonProperty(CRISTIN_BIOBANK_PROJECT_CODE_KEY) String cristinBiobankProjectCodeKey,
                          @JsonProperty(CRISTIN_BIOBANK_TYPE) String cristinBiobankType,
                          @JsonProperty(CRISTIN_BIOBANK_LANGUAGE_KEY) String cristinBiobankLanguage,
                          @JsonProperty(CRISTIN_BIOBANK_PAGE_KEY) String cristinBiobankPageKey,
                          @JsonProperty(CRISTIN_BIOBANK_PER_PAGE_KEY)String cristinBiobankPerPageKey,
                          @JsonProperty(CRISTIN_NAME_FIELD) Map<String, String> name
                          ) {
        this.cristinBiobankId = cristinBiobankId;
        this.cristinBiobankInstitution = cristinBiobankInstitution;
        this.cristinBiobankModifiedSince = cristinBiobankModifiedSince;
        this.cristinBiobankProjectCodeKey = cristinBiobankProjectCodeKey;
        this.cristinBiobankType = cristinBiobankType;
        this.cristinBiobankLanguage = cristinBiobankLanguage;
        this.cristinBiobankPageKey = cristinBiobankPageKey;
        this.cristinBiobankPerPageKey = cristinBiobankPerPageKey;
        this.name = Collections.unmodifiableMap(name);
    }


    public String getCristinBiobankId() {
        return cristinBiobankId;
    }

    public Map<String, String> getName() {
        return name;
    }

    public String getCristinBiobankInstitution() { return cristinBiobankInstitution; }
    public DateInfo getCristinBiobankModifiedSince() { return cristinBiobankModifiedSince; }

    public String getCristinBiobankProjectCodeKey() { return cristinBiobankProjectCodeKey; }
    public String getCristinBiobankType() { return cristinBiobankType; }
    public String getCristinBiobankLanguage() { return cristinBiobankLanguage; }
    public String getCristinBiobankPageKey() { return cristinBiobankPageKey; }
    public String getCristinBiobankPerPageKey() { return cristinBiobankPerPageKey; }


}
