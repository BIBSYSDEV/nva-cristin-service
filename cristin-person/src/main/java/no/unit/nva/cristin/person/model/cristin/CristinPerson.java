package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.person.model.nva.Affiliation;
import no.unit.nva.cristin.person.model.nva.ContactDetails;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.model.TypedLabel;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;

@SuppressWarnings({"unused", "PMD.GodClass"})
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPerson implements JsonSerializable {

    @JsonIgnore
    public static final String CRISTIN_IDENTIFIER = "CristinIdentifier";
    @JsonIgnore
    public static final String ORCID = "ORCID";
    @JsonIgnore
    public static final String FIRST_NAME = "FirstName";
    @JsonIgnore
    public static final String LAST_NAME = "LastName";
    @JsonIgnore
    public static final String PREFERRED_FIRST_NAME = "PreferredFirstName";
    @JsonIgnore
    public static final String PREFERRED_LAST_NAME = "PreferredLastName";

    private String cristinPersonId;
    private CristinOrcid orcid;
    private String firstName;
    private String surname;
    private String firstNamePreferred;
    private String surnamePreferred;
    private String tel;
    private String pictureUrl;
    private List<CristinAffiliation> affiliations;
    private Boolean reserved;
    private String norwegianNationalId;
    private List<CristinPersonEmployment> detailedAffiliations;
    private Boolean identifiedCristinPerson;
    private List<CristinTypedLabel> keywords;
    private Map<String, String> background;

    public String getCristinPersonId() {
        return cristinPersonId;
    }

    public void setCristinPersonId(String cristinPersonId) {
        this.cristinPersonId = cristinPersonId;
    }

    public Optional<CristinOrcid> getOrcid() {
        return Optional.ofNullable(orcid);
    }

    public void setOrcid(CristinOrcid orcid) {
        this.orcid = orcid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Optional<String> getFirstNamePreferred() {
        return Optional.ofNullable(firstNamePreferred);
    }

    public void setFirstNamePreferred(String firstNamePreferred) {
        this.firstNamePreferred = firstNamePreferred;
    }

    public Optional<String> getSurnamePreferred() {
        return Optional.ofNullable(surnamePreferred);
    }

    public void setSurnamePreferred(String surnamePreferred) {
        this.surnamePreferred = surnamePreferred;
    }

    public Optional<String> getTel() {
        return Optional.ofNullable(tel);
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Optional<String> getPictureUrl() {
        return Optional.ofNullable(pictureUrl);
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public List<CristinAffiliation> getAffiliations() {
        return nonNull(affiliations) ? affiliations : Collections.emptyList();
    }

    public void setAffiliations(List<CristinAffiliation> affiliations) {
        this.affiliations = affiliations;
    }


    public Optional<String> getNorwegianNationalId() {
        return Optional.ofNullable(norwegianNationalId);
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

    public List<CristinPersonEmployment> getDetailedAffiliations() {
        return detailedAffiliations;
    }

    public void setDetailedAffiliations(List<CristinPersonEmployment> detailedAffiliations) {
        this.detailedAffiliations = detailedAffiliations;
    }

    public Boolean getIdentifiedCristinPerson() {
        return identifiedCristinPerson;
    }

    public void setIdentifiedCristinPerson(Boolean identifiedCristinPerson) {
        this.identifiedCristinPerson = identifiedCristinPerson;
    }

    public List<CristinTypedLabel> getKeywords() {
        return nonEmptyOrDefault(keywords);
    }

    public void setKeywords(List<CristinTypedLabel> keywords) {
        this.keywords = keywords;
    }

    public Map<String, String> getBackground() {
        return nonEmptyOrDefault(background);
    }

    public void setBackground(Map<String, String> background) {
        this.background = background;
    }

    /**
     * Creates a Nva person model from a Cristin person model. If the person is not publicly viewable, only returns
     * identifier.
     *
     * @return Nva person model.
     */
    public Person toPerson() {
        if (Boolean.TRUE.equals(getReserved())) {
            return new Person.Builder().withId(extractIdUri()).build(); // Also preserving size of hits from upstream
        }
        return new Person.Builder()
                   .withId(extractIdUri())
                   .withIdentifiers(extractNonAuthorizedIdentifiers())
                   .withNames(extractNames())
                   .withContactDetails(extractContactDetails())
                   .withImage(extractImage())
                   .withAffiliations(extractAffiliations())
                   .withVerified(getIdentifiedCristinPerson())
                   .withKeywords(extractKeywords())
                   .withBackground(extractBackground())
                   .build();
    }

    /**
     * Creates a Nva person model from a Cristin person model.
     * This model has additional fields only available to authorized users. It will also show persons not publicly
     * viewable.
     *
     * @return Nva person model.
     */
    public Person toPersonWithAuthorizedFields() {
        return new Person.Builder()
                   .withId(extractIdUri())
                   .withIdentifiers(extractAuthorizedIdentifiers())
                   .withNames(extractNames())
                   .withContactDetails(extractContactDetails())
                   .withImage(extractImage())
                   .withAffiliations(extractAffiliations())
                   .withReserved(getReserved())
                   .withEmployments(extractEmployments())
                   .withVerified(getIdentifiedCristinPerson())
                   .withKeywords(extractKeywords())
                   .withBackground(extractBackground())
                   .build();
    }

    private Set<TypedLabel> extractKeywords() {
        return getKeywords().stream().map(this::toTypedLabel).collect(Collectors.toSet());
    }

    private TypedLabel toTypedLabel(CristinTypedLabel cristinTypedLabel) {
        return new TypedLabel(cristinTypedLabel.getCode(), cristinTypedLabel.getName());
    }

    private Set<TypedValue> extractAuthorizedIdentifiers() {
        return addNinIfPresent(extractNonAuthorizedIdentifiers());
    }

    private ContactDetails extractContactDetails() {
        return getTel().map(ContactDetails::new).orElse(null);
    }

    private URI extractIdUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA)
            .addChild(getCristinPersonId()).getUri();
    }

    private Set<TypedValue> extractNonAuthorizedIdentifiers() {
        Set<TypedValue> identifiers = new HashSet<>();

        identifiers.add(new TypedValue(CRISTIN_IDENTIFIER, getCristinPersonId()));
        getOrcid().flatMap(CristinOrcid::getId).ifPresent(orcid -> identifiers.add(new TypedValue(ORCID, orcid)));

        return identifiers;
    }

    private Set<TypedValue> addNinIfPresent(Set<TypedValue> identifiers) {
        getNorwegianNationalId().ifPresent(nin -> identifiers.add(new TypedValue(NATIONAL_IDENTITY_NUMBER, nin)));
        return identifiers;
    }

    private Set<TypedValue> extractNames() {
        Set<TypedValue> names = new HashSet<>();

        names.add(new TypedValue(FIRST_NAME, getFirstName()));
        names.add(new TypedValue(LAST_NAME, getSurname()));
        getFirstNamePreferred().ifPresent(name -> names.add(new TypedValue(PREFERRED_FIRST_NAME, name)));
        getSurnamePreferred().ifPresent(name -> names.add(new TypedValue(PREFERRED_LAST_NAME, name)));

        return names;
    }

    private URI extractImage() {
        return getPictureUrl().map(UriWrapper::fromUri).map(UriWrapper::getUri).orElse(null);
    }

    private List<Affiliation> extractAffiliations() {
        return getAffiliations().stream().map(CristinAffiliation::toAffiliation).collect(Collectors.toList());
    }

    private Set<Employment> extractEmployments() {
        if (isNull(getDetailedAffiliations())) {
            return Collections.emptySet();
        }
        return getDetailedAffiliations().stream()
                   .map(cristinEmployment -> cristinEmployment.toEmployment(getCristinPersonId()))
                   .collect(Collectors.toSet());
    }

    private Map<String, String> extractBackground() {
        return nonNull(getBackground()) ? new ConcurrentHashMap<>(getBackground()) : Collections.emptyMap();
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
