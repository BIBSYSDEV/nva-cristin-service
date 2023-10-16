package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonNvi(@JsonProperty(VERIFIED_BY) PersonSummary verifiedBy) {

    public static final String VERIFIED_BY = "verifiedBy";

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private PersonSummary verifiedBy;

        private Builder() {
        }

        public Builder withVerifiedBy(PersonSummary verifiedBy) {
            this.verifiedBy = verifiedBy;
            return this;
        }

        public PersonNvi build() {
            return new PersonNvi(verifiedBy);
        }

    }

}
