package no.unit.nva.cristin.projects;

import java.util.List;
import java.util.Map;

class Unit {

    private String cristin_unit_id;
    private Map<String, String> unit_name;
    private String url;
    private Institution institution;

    private Unit parent_unit;
    private List<Unit> subunits;

    String getCristin_unit_id() {
        return cristin_unit_id;
    }

    Map<String, String> getUnit_name() {
        return unit_name;
    }

    String getUrl() {
        return url;
    }

    Institution getInstitution() {
        return institution;
    }

    Unit getParent_unit() {
        return parent_unit;
    }

    List<Unit> getSubunits() {
        return subunits;
    }
}

