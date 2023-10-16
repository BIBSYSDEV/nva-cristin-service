package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public record PersonSummary(@JsonProperty(ID) URI id, @JsonProperty(FIRST_NAME) String firstName,
                            @JsonProperty(LAST_NAME) String lastName) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private URI id;
        private String firstName;
        private String lastName;

        private Builder() {
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public PersonSummary build() {
            return new PersonSummary(id, firstName, lastName);
        }
    }

}
