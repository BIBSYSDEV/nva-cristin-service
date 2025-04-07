package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinOrcid {

    private String id;

    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    public void setId(String id) {
        this.id = id;
    }
}
