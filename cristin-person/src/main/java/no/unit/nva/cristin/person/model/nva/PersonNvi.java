package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.model.Organization;

public record PersonNvi(@JsonProperty(VERIFIED_BY) PersonSummary verifiedBy,
                        @JsonProperty(VERIFIED_AT) Organization verifiedAt) {

    public static final String VERIFIED_BY = "verifiedBy";
    public static final String VERIFIED_AT = "verifiedAt";

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private PersonSummary verifiedBy;
        private Organization verifiedAt;

        private Builder() {
        }

        public Builder withVerifiedBy(PersonSummary verifiedBy) {
            this.verifiedBy = verifiedBy;
            return this;
        }

        public Builder withVerifiedAt(Organization verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public PersonNvi build() {
            return new PersonNvi(verifiedBy, verifiedAt);
        }

    }

}
