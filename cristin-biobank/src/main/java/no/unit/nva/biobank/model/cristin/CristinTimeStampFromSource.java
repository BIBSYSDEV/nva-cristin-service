package no.unit.nva.biobank.model.cristin;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class CristinTimeStampFromSource {

    public static final String DATE = "date";
    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";

    @JsonProperty(DATE)
    private final  Instant date;
    @JsonProperty(CRISTIN_SOURCE_SHORT_NAME)
    private final String sourceShortName;

    public CristinTimeStampFromSource(
            @JsonProperty(DATE) Instant date,
            @JsonProperty(CRISTIN_SOURCE_SHORT_NAME) String sourceShortName) {
        this.date = date;
        this.sourceShortName = sourceShortName;
    }

    public Instant getDate() {
        return date;
    }

    public String getSourceShortName() {
        return sourceShortName;
    }
}
