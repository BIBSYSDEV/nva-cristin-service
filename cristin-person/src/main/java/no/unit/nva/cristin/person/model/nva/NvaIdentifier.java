package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.StringUtils;

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

    // TODO: Is there a better way to create identifiers from CristinPerson?

    /**
     * Transforms identifiers from Cristin into a List.
     *
     * @param cristinPerson Cristin model containing identifiers.
     * @return List of transformed identifiers
     */
    public static List<NvaIdentifier> identifiersFromCristinPerson(CristinPerson cristinPerson) {
        List<NvaIdentifier> identifiers = new ArrayList<>();

        if (!StringUtils.isBlank(cristinPerson.getCristinPersonId())) {
            identifiers.add(new NvaIdentifier.Builder()
                .withType("CristinIdentifier").withValue(cristinPerson.getCristinPersonId()).build());
        }

        if (cristinPerson.getOrcid() != null && !StringUtils.isBlank(cristinPerson.getOrcid().getId())) {
            identifiers.add(new NvaIdentifier.Builder()
                .withType("ORCID").withValue(cristinPerson.getOrcid().getId()).build());
        }

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

        if (!StringUtils.isBlank(cristinPerson.getFirstName())) {
            names.add(new NvaIdentifier.Builder()
                .withType("FirstName").withValue(cristinPerson.getFirstName()).build());
        }

        if (!StringUtils.isBlank(cristinPerson.getSurname())) {
            names.add(new NvaIdentifier.Builder()
                .withType("LastName").withValue(cristinPerson.getSurname()).build());
        }

        if (!StringUtils.isBlank(cristinPerson.getFirstNamePreferred())) {
            names.add(new NvaIdentifier.Builder()
                .withType("PreferredFirstName").withValue(cristinPerson.getFirstNamePreferred()).build());
        }

        if (!StringUtils.isBlank(cristinPerson.getSurnamePreferred())) {
            names.add(new NvaIdentifier.Builder()
                .withType("PreferredLastName").withValue(cristinPerson.getSurnamePreferred()).build());
        }

        return names;
    }
}
