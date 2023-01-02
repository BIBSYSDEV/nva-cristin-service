package no.unit.nva.model.nva;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class TimeStampFromSource {

    public static final String LAST_MODIFIED_DATE = "date";
    public static final String SOURCE_SHORT_NAME = "sourceShortName";

    @JsonProperty(LAST_MODIFIED_DATE)
    private final  Instant lastModifiedDate;
    @JsonProperty(SOURCE_SHORT_NAME)
    private final String sourceShortName;

    public TimeStampFromSource(
            @JsonProperty(LAST_MODIFIED_DATE) Instant lastModifiedDate,
            @JsonProperty(SOURCE_SHORT_NAME) String sourceShortName) {
        this.lastModifiedDate = lastModifiedDate;
        this.sourceShortName = sourceShortName;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getSourceShortName() {
        return sourceShortName;
    }
}

