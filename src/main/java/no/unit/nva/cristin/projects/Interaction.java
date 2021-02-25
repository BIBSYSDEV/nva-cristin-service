package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Interaction {

    @JsonProperty("cristin_person_id")
    public String cristinPersonId;
    public String date;

}

