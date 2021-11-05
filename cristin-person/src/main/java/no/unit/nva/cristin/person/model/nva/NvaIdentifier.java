package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.cristin.person.model.cristin.CristinOrcid;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class NvaIdentifier {

    private final String type;
    private final String value;

    @JsonCreator
    public NvaIdentifier(@JsonProperty("type") String type, @JsonProperty("value") String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaIdentifier)) {
            return false;
        }
        NvaIdentifier that = (NvaIdentifier) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getValue(), that.getValue());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

    @JacocoGenerated
    public static final class Builder {

        private transient String type;
        private transient String value;

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public NvaIdentifier build() {
            return new NvaIdentifier(this.type, this.value);
        }
    }

    /**
     * Transforms identifiers from Cristin into a List.
     *
     * @param cristinPerson Cristin model containing identifiers.
     * @return List of transformed identifiers
     */
    public static List<NvaIdentifier> identifiersFromCristinPerson(CristinPerson cristinPerson) {
        List<NvaIdentifier> identifiers = new ArrayList<>();

        Optional.of(cristinPerson)
            .map(CristinPerson::getCristinPersonId)
            .ifPresent(id -> identifiers.add(createOneIdentifier("CristinIdentifier", id)));

        Optional.of(cristinPerson)
            .map(CristinPerson::getOrcid)
            .map(CristinOrcid::getId)
            .ifPresent(id -> identifiers.add(createOneIdentifier("ORCID", id)));

        return identifiers;
    }

    /**
     * Transforms names from a Cristin person into a List.
     *
     * @param cristinPerson Cristin model containing name identifiers.
     * @return List of transformed names
     */
    public static List<NvaIdentifier> namesFromCristinPerson(CristinPerson cristinPerson) {
        List<NvaIdentifier> names = new ArrayList<>();

        Optional.of(cristinPerson)
            .map(CristinPerson::getFirstName)
            .ifPresent(id -> names.add(createOneIdentifier("FirstName", id)));

        Optional.of(cristinPerson)
            .map(CristinPerson::getSurname)
            .ifPresent(id -> names.add(createOneIdentifier("LastName", id)));

        Optional.of(cristinPerson)
            .map(CristinPerson::getFirstNamePreferred)
            .ifPresent(id -> names.add(createOneIdentifier("PreferredFirstName", id)));

        Optional.of(cristinPerson)
            .map(CristinPerson::getSurnamePreferred)
            .ifPresent(id -> names.add(createOneIdentifier("PreferredLastName", id)));

        return names;
    }

    private static NvaIdentifier createOneIdentifier(String type, String value) {
        return new NvaIdentifier.Builder().withType(type).withValue(value).build();
    }
}
