package no.unit.nva.cristin.organization.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.Map;

public class InstitutionDto {
    /*
    {
      "cristin_institution_id" : "1",
      "institution_name" : {
        "nb" : "Privatperson"
      },
      "acronym" : "PRIVAT",
      "country" : "NO",
      "cristin_user_institution" : false,
      "url" : "https://api.cristin.no/v2/institutions/1"
    }
     */

    @JsonAlias({"cristin_institution_id", "cristin_unit_id"})
    private String id;
    @JsonAlias({"institution_name", "unit_name"})
    private Map<String, String> name;
    @JsonProperty("acronym")
    private String acronym;
    @JsonProperty("country")
    private String country;
    @JsonProperty("cristin_user_institution")
    private boolean cristinUser;
    @JsonProperty("url")
    private URI uri;

    @JsonIgnore
    private URI sourceUri;

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
    public String getAcronym() {
        return acronym;
    }

    @JacocoGenerated
    public void setAcronym(String acronym) {
        this.acronym = acronym;
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
    public URI getUri() {
        return uri;
    }

    @JacocoGenerated
    public void setUri(URI uri) {
        this.uri = uri;
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
