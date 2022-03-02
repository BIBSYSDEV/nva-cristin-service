package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPersonPost extends CristinPerson implements JsonSerializable {

    private String norwegianNationalId;

    @Override
    public String getNorwegianNationalId() {
        return norwegianNationalId;
    }

    @Override
    public void setNorwegianNationalId(String norwegianNationalId) {
        this.norwegianNationalId = norwegianNationalId;
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
