package no.unit.nva.cristin.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.model.DateInfo;

import java.time.Instant;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinDateInfo {

    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";
    public static final String CRISTIN_DATE = "date";

    @JsonInclude(NON_NULL)
    @JsonProperty(CRISTIN_SOURCE_SHORT_NAME)
    private final transient String sourceShortName;
    @JsonProperty(CRISTIN_DATE)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinDateInfo)) {
            return false;
        }
        CristinDateInfo that = (CristinDateInfo) o;
        return Objects.equals(getSourceShortName(), that.getSourceShortName()) && Objects.equals(
            getDate(), that.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceShortName(), getDate());
    }


    public DateInfo toDateInfo() {
        return new DateInfo(sourceShortName,date);
    }

}
