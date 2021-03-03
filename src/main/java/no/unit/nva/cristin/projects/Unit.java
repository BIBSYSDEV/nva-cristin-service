package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_UNIT_ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PARENT_UNIT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.UNIT_NAME;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.ShortClassName")
public class Unit {

    @JsonProperty(CRISTIN_UNIT_ID)
    public String cristinUnitId;
    @JsonProperty(UNIT_NAME)
    public Map<String, String> unitName;
    public String url;
    public Institution institution;

    @JsonProperty(PARENT_UNIT)
    public Unit parentUnit;
    public List<Unit> subunits;

}

