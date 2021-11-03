package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
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
}
