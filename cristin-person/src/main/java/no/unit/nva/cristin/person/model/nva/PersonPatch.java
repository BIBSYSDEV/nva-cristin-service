package no.unit.nva.cristin.person.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.cristin.person.update.PersonPatchSerializer;
import nva.commons.core.JacocoGenerated;

@JsonSerialize(using = PersonPatchSerializer.class)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@JsonInclude(ALWAYS)
@JacocoGenerated
public class PersonPatch {

    private transient Optional<String> orcid;
    private transient Optional<String> firstName;
    private transient Optional<String> lastName;
    private transient Optional<String> preferredFirstName;
    private transient Optional<String> preferredLastName;
    private transient Optional<Boolean> reserved;

    public Optional<String> getOrcid() {
        return orcid;
    }

    public Optional<String> getFirstName() {
        return firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }

    public Optional<String> getPreferredFirstName() {
        return preferredFirstName;
    }

    public Optional<String> getPreferredLastName() {
        return preferredLastName;
    }

    public Optional<Boolean> getReserved() {
        return reserved;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonPatch)) {
            return false;
        }
        PersonPatch that = (PersonPatch) o;
        return Objects.equals(getOrcid(), that.getOrcid())
            && Objects.equals(getFirstName(), that.getFirstName())
            && Objects.equals(getLastName(), that.getLastName())
            && Objects.equals(getPreferredFirstName(), that.getPreferredFirstName())
            && Objects.equals(getPreferredLastName(), that.getPreferredLastName())
            && Objects.equals(getReserved(), that.getReserved());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrcid(), getFirstName(), getLastName(), getPreferredFirstName(), getPreferredLastName(),
            getReserved());
    }
}
