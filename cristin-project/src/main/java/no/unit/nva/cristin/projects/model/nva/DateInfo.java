package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
public class DateInfo implements JsonSerializable {

    public static final String DATE = "date";
    public static final String SOURCE_SHORT_NAME = "sourceShortName";

    @JsonProperty
    @JsonInclude(NON_NULL)
    private final transient String sourceShortName;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private final transient Instant date;

    @JsonCreator
    public DateInfo(@JsonProperty(SOURCE_SHORT_NAME) String sourceShortName, @JsonProperty(DATE) Instant date) {
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
        if (!(o instanceof DateInfo)) {
            return false;
        }
        DateInfo dateInfo = (DateInfo) o;
        return Objects.equals(getSourceShortName(), dateInfo.getSourceShortName()) && Objects.equals(
            getDate(), dateInfo.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceShortName(), getDate());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
