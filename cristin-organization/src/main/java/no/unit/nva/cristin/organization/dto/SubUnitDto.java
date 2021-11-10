package no.unit.nva.cristin.organization.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.Map;

public class SubUnitDto {
    /*
    {
    "cristin_unit_id" : "185.11.0.0",
    "unit_name" : {
      "en" : "Faculty of Theology"
    },
    "url" : "https://api.cristin.no/v2/units/185.11.0.0"
  }
     */

    private String id;
    private Map<String, String> name;
    private Map<String, String> institution;
    private URI uri;
    private String acronym;

    @JsonIgnore
    private URI sourceUri;

    /**
     * Default and JSON constructor.
     *
     * @param id          the Cristin ID.
     * @param name        the unit name.
     * @param institution the institution properties.
     * @param uri         the institution URI.
     * @param acronym     the institution Acronym.
     */
    public SubUnitDto(@JsonProperty("cristin_unit_id") String id,
                      @JsonProperty("unit_name") Map<String, String> name,
                      @JsonProperty("institution") Map<String, String> institution,
                      @JsonProperty("url") String uri,
                      @JsonProperty("acronym") String acronym) {
        this.id = id;
        this.name = name;
        this.institution = institution;
        this.uri = URI.create(uri);
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
    public Map<String, String> getName() {
        return name;
    }

    @JacocoGenerated
    public void setName(Map<String, String> name) {
        this.name = name;
    }

    @JacocoGenerated
    public URI getUri() {
        return uri;
    }

    @JacocoGenerated
    public void setUri(URI uri) {
        this.uri = uri;
    }

    @JacocoGenerated
    public Map<String, String> getInstitution() {
        return institution;
    }

    @JacocoGenerated
    public void setInstitution(Map<String, String> institution) {
        this.institution = institution;
    }

    @JacocoGenerated
    public String getAcronym() {
        return acronym;
    }

    @JacocoGenerated
    public void setAcronym(String acronym) {
        this.acronym = acronym;
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
