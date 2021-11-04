package no.unit.nva.cristin.person.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Person {

    @JsonInclude(NON_NULL)
    private final String context;
    private final URI id;
    private final List<NvaIdentifier> identifiers;
    private final List<NvaIdentifier> names;
    private final ContactDetails contactDetails;
    private final URI image;
    private final List<Affiliation> affiliations;

    /**
     * Creates a Person for serialization to client.
     *
     * @param context        The ontology context.
     * @param id             Identifier of Person.
     * @param identifiers    Different identifiers related to this object.
     * @param names          Different names for this Person.
     * @param contactDetails How to contact this Person.
     * @param image          URI to picture of this Person.
     * @param affiliations   This person's organization affiliations.
     */
    @JsonCreator
    public Person(@JsonProperty("@context") String context, @JsonProperty("id") URI id,
                  @JsonProperty("identifiers") List<NvaIdentifier> identifiers,
                  @JsonProperty("names") List<NvaIdentifier> names,
                  @JsonProperty("contactDetails") ContactDetails contactDetails, @JsonProperty("image") URI image,
                  @JsonProperty("affiliations") List<Affiliation> affiliations) {
        this.context = context;
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

    public URI getId() {
        return id;
    }

    public List<NvaIdentifier> getIdentifiers() {
        return identifiers;
    }

    public List<NvaIdentifier> getNames() {
        return names;
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    public URI getImage() {
        return image;
    }

    public List<Affiliation> getAffiliations() {
        return affiliations;
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
        return getContext().equals(that.getContext())
            && getId().equals(that.getId())
            && getIdentifiers().equals(that.getIdentifiers())
            && getNames().equals(that.getNames())
            && getContactDetails().equals(that.getContactDetails())
            && getImage().equals(that.getImage())
            && getAffiliations().equals(that.getAffiliations());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getIdentifiers(), getNames(), getContactDetails(),
            getImage(), getAffiliations());
    }

    @JacocoGenerated
    public static final class Builder {

        private transient String context;
        private transient URI id;
        private transient List<NvaIdentifier> identifiers;
        private transient List<NvaIdentifier> names;
        private transient ContactDetails contactDetails;
        private transient URI image;
        private transient List<Affiliation> affiliations;

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withIdentifiers(List<NvaIdentifier> identifiers) {
            this.identifiers = identifiers;
            return this;
        }

        public Builder withNames(List<NvaIdentifier> names) {
            this.names = names;
            return this;
        }

        public Builder withContactDetails(ContactDetails contactDetails) {
            this.contactDetails = contactDetails;
            return this;
        }

        public Builder withImage(URI image) {
            this.image = image;
            return this;
        }

        public Builder withAffiliations(List<Affiliation> affiliations) {
            this.affiliations = affiliations;
            return this;
        }

        public Person build() {
            return new Person(this.context, this.id, this.identifiers, this.names, this.contactDetails,
                this.image, this.affiliations);
        }
    }
}
