package no.unit.nva.cristin.organization.dto;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.Map;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
public class SubSubUnitDto {

    private String id;
    private Map<String, String> unitName;
    private InstitutionDto institution;
    private InstitutionDto parentUnit;
    private List<InstitutionDto> parentUnits;
    private List<SubUnitDto> subUnits;
    @JsonIgnore
    private URI sourceUri;
    private String country;
    private String acronym;

    /**
     * The JSON and default creator of the class.
     *
     * @param id          the Cristin id of the unit.
     * @param unitName    the unit name.
     * @param institution the parent institution.
     * @param parentUnit  the direct ascendant of the unit.
     * @param parentUnits the direct and indirect ascendants of the unit.
     * @param subUnits    the (direct) children of the unit.
     * @param country     the country code of the unit.
     * @param acronym     the acronym of the unit.
     */
    @JsonCreator
    public SubSubUnitDto(@JsonProperty("cristin_unit_id") String id,
                         @JsonProperty("unit_name") Map<String, String> unitName,
                         @JsonProperty("institution") InstitutionDto institution,
                         @JsonProperty("parent_unit") InstitutionDto parentUnit,
                         @JsonProperty("parent_units") List<InstitutionDto> parentUnits,
                         @JsonProperty("subunits") List<SubUnitDto> subUnits,
                         @JsonProperty("country") String country,
                         @JsonProperty("acronym") String acronym) {
        this.id = id;
        this.unitName = unitName;
        this.institution = institution;
        this.parentUnit = parentUnit;
        this.parentUnits = parentUnits;
        this.subUnits = subUnits;
        this.country = country;
        this.acronym = acronym;
    }

    @JacocoGenerated
    public String getId() {
        return id;
    }

    @JacocoGenerated
    public void setId(String id) {
        this.id = id;
    }

    @JacocoGenerated
    public Map<String, String> getUnitName() {
        return unitName;
    }

    @JacocoGenerated
    public void setUnitName(Map<String, String> unitName) {
        this.unitName = unitName;
    }

    @JacocoGenerated
    public InstitutionDto getInstitution() {
        return institution;
    }

    @JacocoGenerated
    public void setInstitution(InstitutionDto institution) {
        this.institution = institution;
    }

    @JacocoGenerated
    public InstitutionDto getParentUnit() {
        return parentUnit;
    }

    @JacocoGenerated
    public void setParentUnit(InstitutionDto parentUnit) {
        this.parentUnit = parentUnit;
    }

    @JacocoGenerated
    public List<InstitutionDto> getParentUnits() {
        return parentUnits;
    }

    @JacocoGenerated
    public void setParentUnits(List<InstitutionDto> parentUnits) {
        this.parentUnits = parentUnits;
    }

    @JacocoGenerated
    public List<SubUnitDto> getSubUnits() {
        return subUnits;
    }

    @JacocoGenerated
    public void setSubUnits(List<SubUnitDto> subUnits) {
        this.subUnits = subUnits;
    }

    @JacocoGenerated
    public URI getSourceUri() {
        return sourceUri;
    }

    @JacocoGenerated
    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public static SubSubUnitDto fromJson(String json) {
        return attempt(() -> JsonUtils.dtoObjectMapper.readValue(json, SubSubUnitDto.class)).orElseThrow();
    }



}
