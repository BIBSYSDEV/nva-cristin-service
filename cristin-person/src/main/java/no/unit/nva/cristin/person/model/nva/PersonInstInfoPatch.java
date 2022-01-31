package no.unit.nva.cristin.person.model.nva;

import java.util.Objects;
import java.util.Optional;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@JacocoGenerated
public class PersonInstInfoPatch implements JsonSerializable {

    private transient Optional<String> email;
    private transient Optional<String> phone;

    public Optional<String> getEmail() {
        return email;
    }

    public Optional<String> getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonInstInfoPatch)) {
            return false;
        }
        PersonInstInfoPatch that = (PersonInstInfoPatch) o;
        return Objects.equals(getEmail(), that.getEmail())
            && Objects.equals(getPhone(), that.getPhone());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getPhone());
    }
}
