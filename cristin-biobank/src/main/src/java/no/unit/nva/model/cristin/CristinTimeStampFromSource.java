package no.unit.nva.model.cristin;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class CristinTimeStampFromSource {

    public static final String CRISTIN_LAST_MODIFIED_DATE = "date";
    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";

    @JsonProperty(CRISTIN_LAST_MODIFIED_DATE)
    private final  Instant lastModifiedDate;
    @JsonProperty(CRISTIN_SOURCE_SHORT_NAME)
    private final String sourceShortName;

    public CristinTimeStampFromSource(
            @JsonProperty(CRISTIN_LAST_MODIFIED_DATE) Instant lastModifiedDate,
            @JsonProperty(CRISTIN_SOURCE_SHORT_NAME) String sourceShortName) {
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
