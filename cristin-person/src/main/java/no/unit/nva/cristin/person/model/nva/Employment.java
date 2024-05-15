package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.Constants.HASHTAG;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.model.UriId;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

@JacocoGenerated
public class Employment implements JsonSerializable, UriId {

    @JsonProperty(CONTEXT)
    private String context;
    private URI id;
    private URI type;
    private URI organization;
    private Instant startDate;
    private Instant endDate;
    private Double fullTimeEquivalentPercentage;

    /**
     * Returns an Optional containing either extracted position code if valid type URI otherwise empty.
     */
    public static Optional<String> extractPositionCodeFromTypeUri(URI type) {
        return Optional.ofNullable(type)
            .map(URI::toString)
            .filter(str -> str.contains(HASHTAG))
            .map(str -> str.substring(str.lastIndexOf(HASHTAG) + 1))
            .filter(StringUtils::isNotBlank);
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
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
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getType(), getOrganization(), getStartDate(), getEndDate(),
                            getFullTimeEquivalentPercentage());
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employment that)) {
            return false;
        }
        return Objects.equals(getContext(), that.getContext())
               && Objects.equals(getId(), that.getId())
               && Objects.equals(getType(), that.getType())
               && Objects.equals(getOrganization(), that.getOrganization())
               && Objects.equals(getStartDate(), that.getStartDate())
               && Objects.equals(getEndDate(), that.getEndDate())
               && Objects.equals(getFullTimeEquivalentPercentage(), that.getFullTimeEquivalentPercentage());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    /**
     * Converts this object to a CristinEmployment.
     */
    public CristinPersonEmployment toCristinEmployment() {
        CristinPersonEmployment cristinEmployment = new CristinPersonEmployment();

        cristinEmployment.setAffiliation(generateCristinAffiliation());
        cristinEmployment.setPosition(generateCristinPositionCode());
        cristinEmployment.setStartDate(getStartDate());
        cristinEmployment.setEndDate(getEndDate());
        cristinEmployment.setFtePercentage(getFullTimeEquivalentPercentage());

        return cristinEmployment;
    }

    private CristinOrganization generateCristinAffiliation() {
        String orgId = extractLastPathElement(getOrganization());
        return CristinOrganization.fromIdentifier(orgId);
    }

    private CristinPositionCode generateCristinPositionCode() {
        Optional<String> code = extractPositionCodeFromTypeUri(getType());
        return code.map(CristinPositionCode::new).orElse(null);
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
