package no.unit.nva.cristin.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.model.DateInfo;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinDateInfo(@JsonInclude(NON_NULL) @JsonProperty(CRISTIN_SOURCE_SHORT_NAME) String sourceShortName,
                              @JsonProperty(CRISTIN_DATE) Instant date) {

    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";
    public static final String CRISTIN_DATE = "date";

    public DateInfo toDateInfo() {
        return new DateInfo(sourceShortName(), date());
    }

}
