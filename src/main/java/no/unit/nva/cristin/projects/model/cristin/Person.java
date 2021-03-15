package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Person {

    public String cristinPersonId;
    public String firstName;
    public String surname;
    public String url;
    public List<Role> roles;
}

