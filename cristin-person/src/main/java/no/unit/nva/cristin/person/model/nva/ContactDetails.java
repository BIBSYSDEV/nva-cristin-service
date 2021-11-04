package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class ContactDetails {

    private final String telephone;

    @JsonCreator
    public ContactDetails(@JsonProperty("telephone") String telephone) {
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContactDetails)) {
            return false;
        }
        ContactDetails that = (ContactDetails) o;
        return getTelephone().equals(that.getTelephone());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getTelephone());
    }

    @JacocoGenerated
    public static final class Builder {

        private transient String telephone;

        public Builder withTelephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public ContactDetails build() {
            return new ContactDetails(this.telephone);
        }
    }
}
