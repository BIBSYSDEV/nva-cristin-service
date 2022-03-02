package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonPost;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.common.Utils.distinctByKey;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.*;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AFFILIATIONS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CONTACT_DETAILS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.IDENTIFIERS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.IMAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NAMES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;

@JacocoGenerated
@JsonPropertyOrder({CONTEXT, ID, TYPE, IDENTIFIERS, NAMES, CONTACT_DETAILS, IMAGE, AFFILIATIONS})
public class Person implements JsonSerializable {

    @JsonProperty(TYPE)
    private static final String type = "Person";
    @JsonProperty(ID)
    private URI id;
    @JsonInclude(NON_NULL)
    @JsonProperty(CONTEXT)
    private String context;
    @JsonProperty(IDENTIFIERS)
    private Set<TypedValue> identifiers;
    @JsonProperty(NAMES)
    private Set<TypedValue> names;
    @JsonProperty(CONTACT_DETAILS)
    private ContactDetails contactDetails;
    @JsonProperty(IMAGE)
    private URI image;
    @JsonProperty(AFFILIATIONS)
    private Set<Affiliation> affiliations;
    @JsonProperty(NATIONAL_IDENTITY_NUMBER)
    private String norwegianNationalId;
    @JsonProperty(RESERVED)
    private Boolean reserved;

    private Person() {

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
    public Person(@JsonProperty("id") URI id, @JsonProperty("identifiers") Set<TypedValue> identifiers,
                  @JsonProperty("names") Set<TypedValue> names,
                  @JsonProperty("contactDetails") ContactDetails contactDetails, @JsonProperty("image") URI image,
                  @JsonProperty("affiliations") Set<Affiliation> affiliations) {
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

    public void setContext(String context) {
        this.context = context;
    }

    public Set<TypedValue> getIdentifiers() {
        return Objects.nonNull(identifiers) ? identifiers : Collections.emptySet();
    }

    public URI getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setIdentifiers(Set<TypedValue> identifiers) {
        this.identifiers = identifiers;
    }

    public Set<Affiliation> getAffiliations() {
        return Objects.nonNull(affiliations) ? affiliations : Collections.emptySet();
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    public URI getImage() {
        return image;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public void setAffiliations(Set<Affiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public Set<TypedValue> getNames() {
        return Objects.nonNull(names) ? names : Collections.emptySet();
    }

    public void setContactDetails(ContactDetails contactDetails) {
        this.contactDetails = contactDetails;
    }

    public void setImage(URI image) {
        this.image = image;
    }

    public void setNames(Set<TypedValue> names) {
        this.names = names;
    }

    public String getNorwegianNationalId() {
        return norwegianNationalId;
    }

    public void setNorwegianNationalId(String norwegianNationalId) {
        this.norwegianNationalId = norwegianNationalId;
    }

    public Boolean getReserved() {
        return reserved;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    /**
     * Converts this object to an appropriate format for POST to Cristin.
     */
    public CristinPersonPost toCristinPersonPost() {
        CristinPersonPost cristinPersonPost = new CristinPersonPost();

        Map<String, String> namesMap = convertTypedValuesToMap(getNames());
        cristinPersonPost.setFirstName(namesMap.get(CristinPerson.FIRST_NAME));
        cristinPersonPost.setSurname(namesMap.get(CristinPerson.LAST_NAME));
        cristinPersonPost.setFirstNamePreferred(namesMap.get(CristinPerson.PREFERRED_FIRST_NAME));
        cristinPersonPost.setSurnamePreferred(namesMap.get(CristinPerson.PREFERRED_LAST_NAME));

        Map<String, String> identifierMap = convertTypedValuesToMap(getIdentifiers());
        cristinPersonPost.setNorwegianNationalId(identifierMap.get(NATIONAL_IDENTITY_NUMBER));

        return cristinPersonPost;
    }

    private Map<String, String> convertTypedValuesToMap(Set<TypedValue> typedValueSet) {
        return typedValueSet.stream()
            .filter(TypedValue::hasData)
            .filter(distinctByKey(TypedValue::getType))
            .collect(Collectors.toMap(TypedValue::getType, TypedValue::getValue));
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
            && getIdentifiers().equals(that.getIdentifiers())
            && getNames().equals(that.getNames())
            && Objects.equals(getContactDetails(), that.getContactDetails())
            && Objects.equals(getImage(), that.getImage())
            && getAffiliations().equals(that.getAffiliations());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getIdentifiers(), getNames(), getContactDetails(), getImage(),
            getAffiliations());
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

        public Builder withIdentifiers(Set<TypedValue> identifiers) {
            person.setIdentifiers(identifiers);
            return this;
        }

        public Builder withNames(Set<TypedValue> names) {
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

        public Builder withAffiliations(Set<Affiliation> affiliations) {
            person.setAffiliations(affiliations);
            return this;
        }

        public Builder withNorwegianNationalId(String norwegianNationalId) {
            person.setNorwegianNationalId(norwegianNationalId);
            return this;
        }

        public Builder withReserved(Boolean reserved) {
            person.setReserved(reserved);
            return this;
        }

        public Person build() {
            return person;
        }
    }

}
