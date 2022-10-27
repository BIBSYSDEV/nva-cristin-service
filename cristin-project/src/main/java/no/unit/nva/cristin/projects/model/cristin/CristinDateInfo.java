package no.unit.nva.cristin.projects.model.cristin;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinDateInfo {

    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";
    public static final String CRISTIN_DATE = "date";

    @JsonInclude(NON_NULL)
    private final transient String sourceShortName;
    private final transient Instant date;

    @JsonCreator
    public CristinDateInfo(@JsonProperty(CRISTIN_SOURCE_SHORT_NAME) String sourceShortName,
                           @JsonProperty(CRISTIN_DATE) Instant date) {
        this.sourceShortName = sourceShortName;
        this.date = date;
    }

    public String getSourceShortName() {
        return sourceShortName;
    }

    public Instant getDate() {
        return date;
    }

}
