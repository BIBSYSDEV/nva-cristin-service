package no.unit.nva.cristin.projects;

import java.util.List;
import java.util.Map;

public class Unit {

    String cristin_unit_id;
    Map<String, String> unit_name;
    String url;
    Institution institution;

    Unit parent_unit;
    List<Unit> subunits;

    public String getCristin_unit_id() {
        return cristin_unit_id;
    }

    public Map<String, String> getUnit_name() {
        return unit_name;
    }

    public String getUrl() {
        return url;
    }

    public Institution getInstitution() {
        return institution;
    }

    public Unit getParent_unit() {
        return parent_unit;
    }

    public List<Unit> getSubunits() {
        return subunits;
    }
}

