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
        return Objects.equals(getTelephone(), that.getTelephone());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getTelephone());
    }

}
