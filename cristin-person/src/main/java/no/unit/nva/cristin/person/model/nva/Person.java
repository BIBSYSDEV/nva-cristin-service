package no.unit.nva.cristin.person.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.person.Constants.PERSON_CONTEXT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({"@context"})
public class Person {

    @JsonInclude(NON_NULL)
    @JsonProperty("@context")
    private static String context = PERSON_CONTEXT;
    private URI id;
    private List<TypedValue> identifiers;
    private List<TypedValue> names;
    private ContactDetails contactDetails;
    private URI image;
    private List<Affiliation> affiliations;

    public Person() {

    }

    /**
     * Creates a Person for serialization to client.
     *
     * @param id             Identifier of Person.
     * @param identifiers    Different identifiers related to this object.
     * @param names          Different names for this Person.
     * @param contactDetails How to contact this Person.
     * @param image          URI to picture of this Person.
     * @param affiliations   This person's organization affiliations.
     */
    @JsonCreator
    public Person(@JsonProperty("id") URI id, @JsonProperty("identifiers") List<TypedValue> identifiers,
                  @JsonProperty("names") List<TypedValue> names,
                  @JsonProperty("contactDetails") ContactDetails contactDetails, @JsonProperty("image") URI image,
                  @JsonProperty("affiliations") List<Affiliation> affiliations) {
        this.id = id;
        this.identifiers = identifiers;
        this.names = names;
        this.contactDetails = contactDetails;
        this.image = image;
        this.affiliations = affiliations;
    }

    public String getContext() {
        return context;
    }

    public List<TypedValue> getIdentifiers() {
        return Objects.nonNull(identifiers) ? identifiers : Collections.emptyList();
    }

    public URI getId() {
        return id;
    }

    public void setIdentifiers(List<TypedValue> identifiers) {
        this.identifiers = identifiers;
    }

    public List<Affiliation> getAffiliations() {
        return Objects.nonNull(affiliations) ? affiliations : Collections.emptyList();
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    public URI getImage() {
        return image;
    }

    public void setContext(String thatContext) {
        context = thatContext;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public List<TypedValue> getNames() {
        return Objects.nonNull(names) ? names : Collections.emptyList();
    }

    public void setNames(List<TypedValue> names) {
        this.names = names;
    }

    public void setContactDetails(ContactDetails contactDetails) {
        this.contactDetails = contactDetails;
    }

    public void setImage(URI image) {
        this.image = image;
    }

    public void setAffiliations(List<Affiliation> affiliations) {
        this.affiliations = affiliations;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person)) {
            return false;
        }
        Person that = (Person) o;
        return Objects.equals(getContext(), that.getContext())
            && Objects.equals(getId(), that.getId())
            && sortedListOfIdentifiers(getIdentifiers()).equals(sortedListOfIdentifiers(that.getIdentifiers()))
            && sortedListOfIdentifiers(getNames()).equals(sortedListOfIdentifiers(that.getNames()))
            && Objects.equals(getContactDetails(), that.getContactDetails())
            && Objects.equals(getImage(), that.getImage())
            && sortedListOfAffiliations(getAffiliations()).equals(sortedListOfAffiliations(that.getAffiliations()));
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), sortedListOfIdentifiers(getIdentifiers()),
            sortedListOfIdentifiers(getNames()), getContactDetails(), getImage(),
            sortedListOfAffiliations(getAffiliations()));
    }

    private List<TypedValue> sortedListOfIdentifiers(List<TypedValue> listToSort) {
        return listToSort.stream().sorted(Comparator.comparing(TypedValue::getType)).collect(Collectors.toList());
    }

    private List<Affiliation> sortedListOfAffiliations(List<Affiliation> listToSort) {
        return listToSort.stream().sorted(Comparator.comparing(Affiliation::hashCode)).collect(Collectors.toList());
    }

    @JacocoGenerated
    public static final class Builder {

        private final transient Person person;

        public Builder() {
            person = new Person();
        }

        public Builder withContext(String context) {
            person.setContext(context);
            return this;
        }

        public Builder withId(URI id) {
            person.setId(id);
            return this;
        }

        public Builder withIdentifiers(List<TypedValue> identifiers) {
            person.setIdentifiers(identifiers);
            return this;
        }

        public Builder withNames(List<TypedValue> names) {
            person.setNames(names);
            return this;
        }

        public Builder withContactDetails(ContactDetails contactDetails) {
            person.setContactDetails(contactDetails);
            return this;
        }

        public Builder withImage(URI image) {
            person.setImage(image);
            return this;
        }

        public Builder withAffiliations(List<Affiliation> affiliations) {
            person.setAffiliations(affiliations);
            return this;
        }

        public Person build() {
            return person;
        }
    }

}
