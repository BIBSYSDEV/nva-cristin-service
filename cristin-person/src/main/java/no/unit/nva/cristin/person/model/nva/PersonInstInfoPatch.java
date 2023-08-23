package no.unit.nva.cristin.person.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.person.institution.update.PersonInstInfoPatchSerializer;
import nva.commons.core.JacocoGenerated;

@JsonSerialize(using = PersonInstInfoPatchSerializer.class)
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType"})
@JsonInclude(ALWAYS)
@JacocoGenerated
public class PersonInstInfoPatch implements JsonSerializable {

    private Optional<String> email;
    private Optional<String> phone;

    public Optional<String> getEmail() {
        return email;
    }

    public Optional<String> getPhone() {
        return phone;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonInstInfoPatch that)) {
            return false;
        }
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
