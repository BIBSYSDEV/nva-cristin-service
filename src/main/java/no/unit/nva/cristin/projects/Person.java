package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Person {

    @JsonProperty("cristin_person_id")
    public String cristinPersonId;
    @JsonProperty("first_name")
    public String firstName;
    public String surname;
    public String tel;
    @JsonProperty("identified_cristin_person")
    public Boolean identifiedCristinPerson;
    @JsonProperty("cristin_profile_url")
    public String cristinProfileUrl;
    @JsonProperty("picture_url")
    public String pictureUrl;
    public String url;
    public List<Role> roles;
    public List<Affiliation> affiliations;

}

