package no.unit.nva.model.nva;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.model.cristin.CristinTimeStampFromSource;

import java.time.Instant;

public class TimeStampFromSource {

    public static final String DATE = "date";
    public static final String SOURCE_SHORT_NAME = "sourceShortName";

    @JsonProperty(DATE)
    private final  Instant date;
    @JsonProperty(SOURCE_SHORT_NAME)
    private final String sourceShortName;

    public TimeStampFromSource(
            @JsonProperty(DATE) Instant date,
            @JsonProperty(SOURCE_SHORT_NAME) String sourceShortName) {
        this.date = date;
        this.sourceShortName = sourceShortName;
    }

    public TimeStampFromSource(CristinTimeStampFromSource cristinTimeStampFromSource){
        this.date = cristinTimeStampFromSource.getDate();
        this.sourceShortName = cristinTimeStampFromSource.getSourceShortName();
    }

    public Instant getDate() {
        return date;
    }

    public String getSourceShortName() {
        return sourceShortName;
    }
}

