package no.unit.nva.cristin.person.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.person.Constants.BASE_PATH;
import static no.unit.nva.cristin.person.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.person.Constants.HTTPS;
import static no.unit.nva.cristin.person.Constants.PERSON_CONTEXT;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({"@context"})
public class Person {

    @JsonIgnore
    public static final String PERSON_PATH = "person";

    @JsonInclude(NON_NULL)
    @JsonProperty("@context")
    private static String context = PERSON_CONTEXT;
    private final URI id;
    private final List<NvaIdentifier> identifiers;
    private final List<NvaIdentifier> names;
    private final ContactDetails contactDetails;
    private final URI image;
    private final List<Affiliation> affiliations;

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
    public Person(@JsonProperty("id") URI id, @JsonProperty("identifiers") List<NvaIdentifier> identifiers,
                  @JsonProperty("names") List<NvaIdentifier> names,
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

    /**
     * Creates a Nva person model from a Cristin person model.
     *
     * @param cristinPerson Cristin model.
     * @return Nva person model.
     */
    public static Person fromCristinPerson(CristinPerson cristinPerson) {
        return new Person.Builder()
            .withId(extractIdUri(cristinPerson.getCristinPersonId()))
            .withIdentifiers(NvaIdentifier.identifiersFromCristinPerson(cristinPerson))
            .withNames(NvaIdentifier.namesFromCristinPerson(cristinPerson))
            .withContactDetails(extractContactDetails(cristinPerson))
            .withImage(extractImage(cristinPerson))
            .withAffiliations(extractAffiliations(cristinPerson))
            .build();
    }

    public List<NvaIdentifier> getIdentifiers() {
        return Objects.nonNull(identifiers) ? identifiers : Collections.emptyList();
    }

    public URI getId() {
        return id;
    }

    public List<NvaIdentifier> getNames() {
        return Objects.nonNull(names) ? names : Collections.emptyList();
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

    private static URI extractIdUri(String cristinPersonId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH).addChild(cristinPersonId)
            .getUri();
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

    private static URI extractImage(CristinPerson cristinPerson) {
        return attempt(() -> new URI(cristinPerson.getPictureUrl())).orElse(uriFailure -> null);
    }

    private List<NvaIdentifier> sortedListOfIdentifiers(List<NvaIdentifier> listToSort) {
        return listToSort.stream().sorted(Comparator.comparing(NvaIdentifier::getType)).collect(Collectors.toList());
    }

    public void setContext(String thatContext) {
        context = thatContext;
    }

    @JacocoGenerated
    public static final class Builder {

        private transient URI id;
        private transient List<NvaIdentifier> identifiers;
        private transient List<NvaIdentifier> names;
        private transient ContactDetails contactDetails;
        private transient URI image;
        private transient List<Affiliation> affiliations;

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
            return new Person(this.id, this.identifiers, this.names, this.contactDetails,
                this.image, this.affiliations);
        }
    }

    private static ContactDetails extractContactDetails(CristinPerson cristinPerson) {
        return new ContactDetails.Builder().withTelephone(cristinPerson.getTel()).build();
    }

    private List<Affiliation> sortedListOfAffiliations(List<Affiliation> listToSort) {
        return listToSort.stream().sorted(Comparator.comparing(Affiliation::hashCode)).collect(Collectors.toList());
    }

    private static List<Affiliation> extractAffiliations(CristinPerson cristinPerson) {
        return cristinPerson.getAffiliations().stream().map(Affiliation::fromCristinAffiliation).collect(
            Collectors.toList());
    }
}
