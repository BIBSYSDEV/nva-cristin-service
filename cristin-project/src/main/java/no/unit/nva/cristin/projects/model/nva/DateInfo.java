package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class DateInfo implements JsonSerializable {

    @JsonProperty
    @JsonInclude(NON_NULL)
    private String sourceShortName;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private Instant date;

    public DateInfo() {

    }

    public DateInfo(String sourceShortName, Instant date) {
        this.sourceShortName = sourceShortName;
        this.date = date;
    }

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
