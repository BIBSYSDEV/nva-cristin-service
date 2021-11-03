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
    private final String type;
    private final List<NvaIdentifier> identifiers;
    private final List<NvaIdentifier> names;
    private final ContactDetails contactDetails;
    private final URI image;
    private final List<PersonAffiliation> affiliations;

    /**
     * Creates a Person for serialization to client.
     *
     * @param context        The ontology context.
     * @param id             Identifier of Person.
     * @param type           Type of object, always Person.
     * @param identifiers    Different identifiers related to this object.
     * @param names          Different names for this Person.
     * @param contactDetails How to contact this Person.
     * @param image          URI to picture of this Person.
     * @param affiliations   This person's organization affiliations.
     */
    @JsonCreator
    public Person(@JsonProperty("@context") String context, @JsonProperty("id") URI id,
                  @JsonProperty("type") String type, @JsonProperty("identifiers") List<NvaIdentifier> identifiers,
                  @JsonProperty("names") List<NvaIdentifier> names,
                  @JsonProperty("contactDetails") ContactDetails contactDetails, @JsonProperty("image") URI image,
                  @JsonProperty("affiliations") List<PersonAffiliation> affiliations) {
        this.context = context;
        this.id = id;
        this.type = type;
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

    public String getType() {
        return type;
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

    public List<PersonAffiliation> getAffiliations() {
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
            && getType().equals(that.getType())
            && getIdentifiers().equals(that.getIdentifiers())
            && getNames().equals(that.getNames())
            && getContactDetails().equals(that.getContactDetails())
            && getImage().equals(that.getImage())
            && getAffiliations().equals(that.getAffiliations());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getType(), getIdentifiers(), getNames(), getContactDetails(),
            getImage(), getAffiliations());
    }
}
