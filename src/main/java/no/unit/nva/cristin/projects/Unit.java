package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.ShortClassName")
public class Unit {

    @SerializedName("cristin_unit_id")
    public String cristinUnitId;
    @SerializedName("unit_name")
    public Map<String, String> unitName;
    public String url;
    public Institution institution;

    @SerializedName("parent_unit")
    public Unit parentUnit;
    public List<Unit> subunits;

}

