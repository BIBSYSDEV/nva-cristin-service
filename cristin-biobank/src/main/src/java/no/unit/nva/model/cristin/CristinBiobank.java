package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

public final class CristinBiobank {
    //Here be more params
    private static final String CRISTIN_BIOBANK_ID = "cristin_biobank_id";
    private static final String NAME_FIELD = "name";

    @JsonProperty(CRISTIN_BIOBANK_ID)
    private final String cristin_biobank_id;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> name;


    public CristinBiobank(@JsonProperty(CRISTIN_BIOBANK_ID) String cristin_biobank_id,
                          @JsonProperty(NAME_FIELD) Map<String, String> name) {
        this.cristin_biobank_id = cristin_biobank_id;
        this.name  = Collections.unmodifiableMap(name);
    }

    public String getCristin_biobank_id() {
        return cristin_biobank_id;
    }

    public Map<String, String> getName() {
        return name;
    }

}
