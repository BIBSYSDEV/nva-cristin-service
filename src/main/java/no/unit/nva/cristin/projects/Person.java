package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_PERSON_ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_PROFILE_URL;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.IDENTIFIED_CRISTIN_PERSON;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PICTURE_URL;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Person {

    @JsonProperty(CRISTIN_PERSON_ID)
    public String cristinPersonId;
    @JsonProperty(FIRST_NAME)
    public String firstName;
    public String surname;
    public String tel;
    @JsonProperty(IDENTIFIED_CRISTIN_PERSON)
    public Boolean identifiedCristinPerson;
    @JsonProperty(CRISTIN_PROFILE_URL)
    public String cristinProfileUrl;
    @JsonProperty(PICTURE_URL)
    public String pictureUrl;
    public String url;
    public List<Role> roles;
    public List<Affiliation> affiliations;

}

