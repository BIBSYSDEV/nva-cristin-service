package no.unit.nva.cristin.projects.model.cristin;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinDateInfo {

    @JsonInclude(NON_NULL)
    private String sourceShortName;
    private Instant date;

    public String getSourceShortName() {
        return sourceShortName;
    }

    public void setSourceShortName(String sourceShortName) {
        this.sourceShortName = sourceShortName;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

}
