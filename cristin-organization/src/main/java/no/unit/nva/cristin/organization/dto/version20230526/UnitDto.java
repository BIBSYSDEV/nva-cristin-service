package no.unit.nva.cristin.organization.dto.version20230526;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.organization.dto.InstitutionDto;

public class UnitDto implements JsonSerializable {

    private final String id;
    private final Map<String, String> unitName;
    private final InstitutionDto institution;
    private final String acronym;
    private UnitDto parentUnit;
    private final List<UnitDto> parentUnits;
    private List<UnitDto> subUnits;
    private final URI uri;

    /**
     * Default and JSON constructor.
     */
    @JsonCreator
    public UnitDto(@JsonProperty("cristin_unit_id") String id,
                   @JsonProperty("unit_name") Map<String, String> unitName,
                   @JsonProperty("institution") InstitutionDto institution,
                   @JsonProperty("acronym") String acronym,
                   @JsonProperty("parent_unit") UnitDto parentUnit,
                   @JsonProperty("parent_units") List<UnitDto> parentUnits,
                   @JsonProperty("subunits") List<UnitDto> subUnits,
                   @JsonProperty("url") URI uri) {
        this.id = id;
        this.unitName = unitName;
        this.institution = institution;
        this.acronym = acronym;
        this.parentUnit = parentUnit;
        this.parentUnits = parentUnits;
        this.subUnits = subUnits;
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getUnitName() {
        return nonEmptyOrDefault(unitName);
    }

    public InstitutionDto getInstitution() {
        return institution;
    }

    public String getAcronym() {
        return acronym;
    }

    public UnitDto getParentUnit() {
        return parentUnit;
    }

    public void setParentUnit(UnitDto parentUnit) {
        this.parentUnit = parentUnit;
    }

    public List<UnitDto> getParentUnits() {
        return nonEmptyOrDefault(parentUnits);
    }

    public List<UnitDto> getSubUnits() {
        return nonEmptyOrDefault(subUnits);
    }

    public void setSubUnits(List<UnitDto> subUnits) {
        this.subUnits = subUnits;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
