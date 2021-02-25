package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.ShortClassName")
public class Unit {

    @JsonProperty("cristin_unit_id")
    public String cristinUnitId;
    @JsonProperty("unit_name")
    public Map<String, String> unitName;
    public String url;
    public Institution institution;

    @JsonProperty("parent_unit")
    public Unit parentUnit;
    public List<Unit> subunits;

}

