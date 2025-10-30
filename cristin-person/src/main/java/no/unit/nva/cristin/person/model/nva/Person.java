package no.unit.nva.cristin.person.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIERS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AFFILIATIONS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AWARDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COLLABORATION;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CONTACT_DETAILS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COUNTRIES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.IMAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NAMES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PLACE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.VERIFIED;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.adapter.PersonToCristinFormat;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.model.UriId;

@SuppressWarnings("PMD.TooManyFields")
@JsonPropertyOrder({CONTEXT, ID, TYPE, IDENTIFIERS, NAMES, CONTACT_DETAILS, IMAGE, AFFILIATIONS, RESERVED, EMPLOYMENTS,
    VERIFIED, KEYWORDS, BACKGROUND, NVI, PLACE, COLLABORATION, COUNTRIES, AWARDS})
public record Person(@JsonProperty(CONTEXT) String context,
                     @JsonProperty(ID) URI id,
                     @JsonProperty(IDENTIFIERS) Set<TypedValue> identifiers,
                     @JsonProperty(NAMES) Set<TypedValue> names,
                     @JsonProperty(CONTACT_DETAILS) ContactDetails contactDetails,
                     @JsonProperty(IMAGE) URI image,
                     @JsonProperty(AFFILIATIONS) List<Affiliation> affiliations,
                     @JsonProperty(RESERVED) Boolean reserved,
                     @JsonProperty(EMPLOYMENTS) Set<Employment> employments,
                     @JsonProperty(VERIFIED) boolean verified,
                     @JsonProperty(KEYWORDS) Set<TypedLabel> keywords,
                     @JsonProperty(BACKGROUND) Map<String, String> background,
                     @JsonProperty(NVI) PersonNvi nvi,
                     @JsonProperty(PLACE) Map<String, String> place,
                     @JsonProperty(COLLABORATION) Map<String, String> collaboration,
                     @JsonProperty(COUNTRIES) Set<TypedLabel> countries,
                     @JsonProperty(AWARDS) Set<Award> awards) implements JsonSerializable, UriId {

    public static final String type = "Person";

    @JsonProperty(TYPE)
    public String type() {
        return type;
    }

    @JsonInclude(NON_NULL)
    @Override
    public String context() {
        return context;
    }

    @Override
    public Set<TypedValue> identifiers() {
        return nonEmptyOrDefault(identifiers);
    }

    @Override
    public List<Affiliation> affiliations() {
        return nonEmptyOrDefault(affiliations);
    }

    @Override
    public Set<TypedValue> names() {
        return nonEmptyOrDefault(names);
    }

    @Override
    public Set<TypedLabel> keywords() {
        return nonEmptyOrDefault(keywords);
    }

    @Override
    public Map<String, String> background() {
        return nonEmptyOrDefault(background);
    }

    @Override
    public Map<String, String> place() {
        return nonEmptyOrDefault(place);
    }

    @Override
    public Map<String, String> collaboration() {
        return nonEmptyOrDefault(collaboration);
    }

    @Override
    public Set<TypedLabel> countries() {
        return nonEmptyOrDefault(countries);
    }

    @Override
    public Set<Award> awards() {
        return nonEmptyOrDefault(awards);
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public URI getId() {
        return id;
    }

    public CristinPerson toCristinPerson() {
        return new PersonToCristinFormat().apply(this);
    }

    public static final class Builder {

        private String context;
        private URI id;
        private Set<TypedValue> identifiers;
        private Set<TypedValue> names;
        private ContactDetails contactDetails;
        private URI image;
        private List<Affiliation> affiliations;
        private Boolean reserved;
        private Set<Employment> employments;
        private boolean verified;
        private Set<TypedLabel> keywords;
        private Map<String, String> background;
        private PersonNvi nvi;
        private Map<String, String> place;
        private Map<String, String> collaboration;
        private Set<TypedLabel> countries;
        private Set<Award> awards;

        public Builder() {

        }

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withIdentifiers(Set<TypedValue> identifiers) {
            this.identifiers = identifiers;
            return this;
        }

        public Builder withNames(Set<TypedValue> names) {
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

        public Builder withReserved(Boolean reserved) {
            this.reserved = reserved;
            return this;
        }

        public Builder withEmployments(Set<Employment> employments) {
            this.employments = employments;
            return this;
        }

        public Builder withVerified(Boolean verified) {
            this.verified = Boolean.TRUE.equals(verified);
            return this;
        }

        public Builder withKeywords(Set<TypedLabel> keywords) {
            this.keywords = keywords;
            return this;
        }

        public Builder withBackground(Map<String, String> background) {
            this.background = background;
            return this;
        }

        public Builder withNvi(PersonNvi nvi) {
            this.nvi = nvi;
            return this;
        }

        public Builder withPlace(Map<String, String> place) {
            this.place = place;
            return this;
        }

        public Builder withCollaboration(Map<String, String> collaboration) {
            this.collaboration = collaboration;
            return this;
        }

        public Builder withCountries(Set<TypedLabel> countries) {
            this.countries = countries;
            return this;
        }

        public Builder withAwards(Set<Award> awards) {
            this.awards = awards;
            return this;
        }

        public Person build() {
            return new Person(context, id, identifiers, names, contactDetails, image, affiliations, reserved,
                              employments, verified, keywords, background, nvi, place, collaboration, countries,
                              awards);
        }

    }

    public Builder copy() {
        return new Builder().withContext(context)
                            .withId(id)
                            .withIdentifiers(identifiers)
                            .withNames(names)
                            .withContactDetails(contactDetails)
                            .withImage(image)
                            .withAffiliations(affiliations)
                            .withReserved(reserved)
                            .withEmployments(employments)
                            .withVerified(verified)
                            .withKeywords(keywords)
                            .withBackground(background)
                            .withNvi(nvi)
                            .withPlace(place)
                            .withCollaboration(collaboration)
                            .withCountries(countries)
                            .withAwards(awards);
    }

}
