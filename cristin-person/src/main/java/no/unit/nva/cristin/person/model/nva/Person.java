package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.cristin.CristinNviInstitutionUnit;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.cristin.CristinPersonNvi;
import no.unit.nva.cristin.person.model.cristin.CristinPersonSummary;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.distinctByKey;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIERS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AFFILIATIONS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CONTACT_DETAILS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.IMAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NAMES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.VERIFIED;

@JacocoGenerated
@SuppressWarnings("PMD.ExcessivePublicCount")
@JsonPropertyOrder({CONTEXT, ID, TYPE, IDENTIFIERS, NAMES, CONTACT_DETAILS, IMAGE, AFFILIATIONS, RESERVED, EMPLOYMENTS,
    VERIFIED, KEYWORDS, BACKGROUND})
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
    private List<Affiliation> affiliations;
    @JsonProperty(RESERVED)
    private Boolean reserved;
    @JsonProperty(EMPLOYMENTS)
    private Set<Employment> employments;
    @JsonProperty(VERIFIED)
    private Boolean verified;
    @JsonProperty(KEYWORDS)
    private Set<TypedLabel> keywords;
    @JsonProperty(BACKGROUND)
    private Map<String, String> background;
    @JsonProperty(NVI)
    private PersonNvi nvi;

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
     * @param reserved       If person is a reserved person, meaning not publicly viewable.
     * @param employments    This person's detailed employment data at each organization.
     * @param verified       If this person is a verified person.
     * @param keywords       Keywords related to this person.
     * @param background     Background information about this person.
     * @param nvi            NVI information about this person.
     */
    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public Person(@JsonProperty(ID) URI id, @JsonProperty(IDENTIFIERS) Set<TypedValue> identifiers,
                  @JsonProperty(NAMES) Set<TypedValue> names,
                  @JsonProperty(CONTACT_DETAILS) ContactDetails contactDetails, @JsonProperty(IMAGE) URI image,
                  @JsonProperty(AFFILIATIONS) List<Affiliation> affiliations, @JsonProperty(RESERVED) Boolean reserved,
                  @JsonProperty(EMPLOYMENTS) Set<Employment> employments, @JsonProperty(VERIFIED) Boolean verified,
                  @JsonProperty(KEYWORDS) Set<TypedLabel> keywords,
                  @JsonProperty(BACKGROUND) Map<String, String> background, @JsonProperty(NVI) PersonNvi nvi) {
        this.id = id;
        this.identifiers = identifiers;
        this.names = names;
        this.contactDetails = contactDetails;
        this.image = image;
        this.affiliations = affiliations;
        this.reserved = reserved;
        this.employments = employments;
        this.verified = verified;
        this.keywords = keywords;
        this.background = background;
        this.nvi = nvi;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Set<TypedValue> getIdentifiers() {
        return nonNull(identifiers) ? identifiers : Collections.emptySet();
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

    public List<Affiliation> getAffiliations() {
        return nonNull(affiliations) ? affiliations : Collections.emptyList();
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

    public void setAffiliations(List<Affiliation> affiliations) {
        this.affiliations = affiliations;
    }

    public Set<TypedValue> getNames() {
        return nonNull(names) ? names : Collections.emptySet();
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

    public Boolean getReserved() {
        return reserved;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public Set<Employment> getEmployments() {
        return employments;
    }

    public void setEmployments(Set<Employment> employments) {
        this.employments = employments;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Set<TypedLabel> getKeywords() {
        return nonEmptyOrDefault(keywords);
    }

    public void setKeywords(Set<TypedLabel> keywords) {
        this.keywords = keywords;
    }

    public Map<String, String> getBackground() {
        return nonEmptyOrDefault(background);
    }

    public void setBackground(Map<String, String> background) {
        this.background = background;
    }

    public PersonNvi getNvi() {
        return nvi;
    }

    public void setNvi(PersonNvi nvi) {
        this.nvi = nvi;
    }

    /**
     * Converts this object to an appropriate format for POST to Cristin.
     */
    public CristinPerson toCristinPerson() {
        var cristinPerson = new CristinPerson();

        var namesMap = convertTypedValuesToMap(getNames());
        cristinPerson.setFirstName(namesMap.get(CristinPerson.FIRST_NAME));
        cristinPerson.setSurname(namesMap.get(CristinPerson.LAST_NAME));
        cristinPerson.setFirstNamePreferred(namesMap.get(CristinPerson.PREFERRED_FIRST_NAME));
        cristinPerson.setSurnamePreferred(namesMap.get(CristinPerson.PREFERRED_LAST_NAME));

        var identifierMap = convertTypedValuesToMap(getIdentifiers());
        cristinPerson.setNorwegianNationalId(identifierMap.get(NATIONAL_IDENTITY_NUMBER));

        cristinPerson.setDetailedAffiliations(mapEmploymentsToCristinEmployments(getEmployments()));
        cristinPerson.setReserved(getReserved());
        cristinPerson.setKeywords(extractKeywordCodes(getKeywords()));
        cristinPerson.setBackground(getBackground());

        if (nonNull(getNvi())) {
            CristinPersonNvi cristinPersonNvi = new CristinPersonNvi(extractVerifiedById(), extractVerifiedAtId(), null);
            cristinPerson.setPersonNvi(cristinPersonNvi);
        }

        return cristinPerson;
    }

    private CristinNviInstitutionUnit extractVerifiedAtId() {
        var organizationHavingVerifiedId = UriUtils.extractLastPathElement(getNvi().verifiedAt().getId());
        return new CristinNviInstitutionUnit(null,
                                             CristinUnit.fromCristinUnitIdentifier(organizationHavingVerifiedId));
    }

    private CristinPersonSummary extractVerifiedById() {
        var personHavingVerifiedId = UriUtils.extractLastPathElement(getNvi().verifiedBy().id());
        return CristinPersonSummary.builder().withCristinPersonId(personHavingVerifiedId).build();
    }

    private List<CristinTypedLabel> extractKeywordCodes(Set<TypedLabel> keywords) {
        return keywords.stream()
            .map(label -> new CristinTypedLabel(label.getType(), null))
            .collect(Collectors.toList());
    }

    private Map<String, String> convertTypedValuesToMap(Set<TypedValue> typedValueSet) {
        return typedValueSet.stream()
            .filter(TypedValue::hasData)
            .filter(distinctByKey(TypedValue::getType))
            .collect(Collectors.toMap(TypedValue::getType, TypedValue::getValue));
    }

    /**
     * Converts NVA formatted employments to Cristin formatted employments.
     */
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public static List<CristinPersonEmployment> mapEmploymentsToCristinEmployments(Set<Employment> employments) {
        if (isNull(employments)) {
            return null;
        }
        return
            employments
                .stream()
                .map(Employment::toCristinEmployment)
                .toList();
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person person)) {
            return false;
        }
        return Objects.equals(getId(), person.getId())
               && Objects.equals(getContext(), person.getContext())
               && Objects.equals(getIdentifiers(), person.getIdentifiers())
               && Objects.equals(getNames(), person.getNames())
               && Objects.equals(getContactDetails(), person.getContactDetails())
               && Objects.equals(getImage(), person.getImage())
               && Objects.equals(getAffiliations(), person.getAffiliations())
               && Objects.equals(getReserved(), person.getReserved())
               && Objects.equals(getEmployments(), person.getEmployments())
               && Objects.equals(getVerified(), person.getVerified())
               && Objects.equals(getKeywords(), person.getKeywords())
               && Objects.equals(getBackground(), person.getBackground())
               && Objects.equals(getNvi(), person.getNvi());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getContext(), getIdentifiers(), getNames(), getContactDetails(), getImage(),
                            getAffiliations(), getReserved(), getEmployments(), getVerified(), getKeywords(),
                            getBackground(), getNvi());
    }

    @Override
    public String toString() {
        return toJsonString();
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

        public Builder withAffiliations(List<Affiliation> affiliations) {
            person.setAffiliations(affiliations);
            return this;
        }

        public Builder withReserved(Boolean reserved) {
            person.setReserved(reserved);
            return this;
        }

        public Builder withEmployments(Set<Employment> employments) {
            person.setEmployments(employments);
            return this;
        }

        public Builder withVerified(Boolean verified) {
            person.setVerified(verified);
            return this;
        }

        public Builder withKeywords(Set<TypedLabel> keywords) {
            person.setKeywords(keywords);
            return this;
        }

        public Builder withBackground(Map<String, String> background) {
            person.setBackground(background);
            return this;
        }

        public Builder withNvi(PersonNvi nvi) {
            person.setNvi(nvi);
            return this;
        }

        public Person build() {
            return person;
        }
    }

}
