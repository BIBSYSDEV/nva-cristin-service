package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CristinPersonFacet extends CristinFacet {

    private final String cristinPersonId;
    private final String name;

    public CristinPersonFacet(@JsonProperty("cristin_person_id") String cristinPersonId,
                              @JsonProperty("name") String name) {
        super();
        this.cristinPersonId = cristinPersonId;
        this.name = name;
    }

    @Override
    public String getKey() {
        return cristinPersonId;
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of("en", name);
    }

}
