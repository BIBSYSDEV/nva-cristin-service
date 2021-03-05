package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_PERSON_ID;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Interaction {

    @JsonProperty(CRISTIN_PERSON_ID)
    public String cristinPersonId;
    public String date;

}

