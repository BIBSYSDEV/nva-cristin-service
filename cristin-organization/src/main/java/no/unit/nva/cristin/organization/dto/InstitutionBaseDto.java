package no.unit.nva.cristin.organization.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Class representing a call to <pre>https://api.cristin.no/v2/institutions/</pre>. It represents an "Institution" unit.
 * Examples of such institutions are NTNU, UIO etc. The corresponding unit is the organizational unit tha corresponds to
 * this institution.
 */
public class InstitutionBaseDto {

    /*
    /*
{
  "cristin_institution_id" : "185",
  "institution_name" : {
    "en" : "University of Oslo"
  },
  "acronym" : "UIO",
  "country" : "NO",
  "cristin_user_institution" : true,
  "corresponding_unit" : {
    "cristin_unit_id" : "185.90.0.0",
    "url" : "https://api.cristin.no/v2/units/185.90.0.0"
  }
}
 */
    @JsonAlias("cristin_unit_id")
    private String id;
    @JsonAlias("unit_name")
    private Map<String, String> name;
    private String shortName;
    private String country;
    private boolean cristinUser;
    private CorrespondingUnitDto correspondingUnitDto;
    private List<SubSubUnitDto> subUnitDtoList;
    private InstitutionDto institution;

    @JsonIgnore
    private URI sourceUri;

    /**
     * The JSON and default constructor.
     *
     * @param name                 the institution name.
     * @param institution          the institution properties.
     * @param cristinUser          true if the institution is a cristin user.
     * @param id                   the Cristin id of the institution.
     * @param shortName            the short name of the institution.
     * @param country              the institution's country.
     * @param correspondingUnitDto the logical administrative unit of the institution.
     * @param subUnitDtoList       the list of the units belonging to the institution.
     */
    @JsonCreator
    public InstitutionBaseDto(@JsonProperty("institution_name") Map<String, String> name,
                              @JsonProperty("institution") InstitutionDto institution,
                              @JsonProperty("cristin_user_institution") boolean cristinUser,
                              @JsonProperty("cristin_institution_id") String id,
                              @JsonProperty("acronym") String shortName,
                              @JsonProperty("country") String country,
                              @JsonProperty("corresponding_unit") CorrespondingUnitDto correspondingUnitDto,
                              @JsonProperty("subunits")
                                  List<SubSubUnitDto> subUnitDtoList) {
        this.name = name;
        this.institution = institution;
        this.cristinUser = cristinUser;
        this.id = id;
        this.shortName = shortName;
        this.country = country;
        this.correspondingUnitDto = correspondingUnitDto;
        this.subUnitDtoList = subUnitDtoList;
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
    public Map<String, String> getName() {
        return name;
    }

    @JacocoGenerated
    public void setName(Map<String, String> name) {
        this.name = name;
    }

    @JacocoGenerated
    public String getShortName() {
        return shortName;
    }

    @JacocoGenerated
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @JacocoGenerated
    public String getCountry() {
        return country;
    }

    @JacocoGenerated
    public void setCountry(String country) {
        this.country = country;
    }

    @JacocoGenerated
    public boolean isCristinUser() {
        return cristinUser;
    }

    @JacocoGenerated
    public void setCristinUser(boolean cristinUser) {
        this.cristinUser = cristinUser;
    }

    @JacocoGenerated
    public CorrespondingUnitDto getCorrespondingUnitDto() {
        return correspondingUnitDto;
    }

    @JacocoGenerated
    public void setCorrespondingUnitDto(CorrespondingUnitDto correspondingUnitDto) {
        this.correspondingUnitDto = correspondingUnitDto;
    }

    @JacocoGenerated
    public List<SubSubUnitDto> getSubUnitDtoList() {
        return subUnitDtoList;
    }

    @JacocoGenerated
    public void setSubUnitDtoList(List<SubSubUnitDto> subUnitDtoList) {
        this.subUnitDtoList = subUnitDtoList;
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
    public URI getSourceUri() {
        return sourceUri;
    }

    @JacocoGenerated
    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }
}
