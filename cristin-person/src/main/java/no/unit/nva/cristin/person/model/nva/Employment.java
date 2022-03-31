package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@JacocoGenerated
public class Employment implements JsonSerializable {

    @JsonProperty(CONTEXT)
    private String context;
    private URI id;
    private URI type;
    private URI organization;
    private Instant startDate;
    private Instant endDate;
    private Double fullTimeEquivalentPercentage;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public URI getType() {
        return type;
    }

    public void setType(URI type) {
        this.type = type;
    }

    public URI getOrganization() {
        return organization;
    }

    public void setOrganization(URI organization) {
        this.organization = organization;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Double getFullTimeEquivalentPercentage() {
        return fullTimeEquivalentPercentage;
    }

    public void setFullTimeEquivalentPercentage(Double fullTimeEquivalentPercentage) {
        this.fullTimeEquivalentPercentage = fullTimeEquivalentPercentage;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employment)) {
            return false;
        }
        Employment that = (Employment) o;
        return Objects.equals(getContext(), that.getContext())
            && Objects.equals(getId(), that.getId())
            && Objects.equals(getType(), that.getType())
            && Objects.equals(getOrganization(), that.getOrganization())
            && Objects.equals(getStartDate(), that.getStartDate())
            && Objects.equals(getEndDate(), that.getEndDate())
            && Objects.equals(getFullTimeEquivalentPercentage(), that.getFullTimeEquivalentPercentage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getType(), getOrganization(), getStartDate(), getEndDate(),
            getFullTimeEquivalentPercentage());
    }

    public static final class Builder {

        private final transient Employment employment;

        public Builder() {
            employment = new Employment();
        }

        public Builder withContext(String context) {
            employment.setContext(context);
            return this;
        }

        public Builder withId(URI id) {
            employment.setId(id);
            return this;
        }

        public Builder withType(URI type) {
            employment.setType(type);
            return this;
        }

        public Builder withOrganization(URI organization) {
            employment.setOrganization(organization);
            return this;
        }

        public Builder withStartDate(Instant startDate) {
            employment.setStartDate(startDate);
            return this;
        }

        public Builder withEndDate(Instant endDate) {
            employment.setEndDate(endDate);
            return this;
        }

        public Builder withFullTimeEquivalentPercentage(Double fullTimeEquivalentPercentage) {
            employment.setFullTimeEquivalentPercentage(fullTimeEquivalentPercentage);
            return this;
        }

        public Employment build() {
            return employment;
        }
    }
}
