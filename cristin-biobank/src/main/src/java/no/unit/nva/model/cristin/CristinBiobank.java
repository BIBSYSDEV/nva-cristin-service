package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

public final class CristinBiobank {
    //Here be more params
    private static final String CRISTIN_BIOBANK_ID = "cristin_biobank_id";
    private static final String NAME_FIELD = "name";

    @JsonProperty(CRISTIN_BIOBANK_ID)
    private final String cristinBiobankId;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> name;


    public CristinBiobank(@JsonProperty(CRISTIN_BIOBANK_ID) String cristinBiobankId,
                          @JsonProperty(NAME_FIELD) Map<String, String> name) {
        this.cristinBiobankId = cristinBiobankId;
        this.name  = Collections.unmodifiableMap(name);
    }

    public String getCristinBiobankId() {
        return cristinBiobankId;
    }

    public Map<String, String> getName() {
        return name;
    }

}
