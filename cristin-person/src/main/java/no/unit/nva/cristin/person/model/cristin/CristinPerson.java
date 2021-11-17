package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.person.Constants.BASE_PATH;
import static no.unit.nva.cristin.person.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.person.Constants.HTTPS;
import static no.unit.nva.cristin.person.Constants.PERSON_PATH;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.cristin.person.model.nva.Affiliation;
import no.unit.nva.cristin.person.model.nva.ContactDetails;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPerson {

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
        return Objects.nonNull(affiliations) ? affiliations : Collections.emptyList();
    }

    public void setAffiliations(List<CristinAffiliation> affiliations) {
        this.affiliations = affiliations;
    }

    /**
     * Creates a Nva person model from a Cristin person model.
     *
     * @return Nva person model.
     */
    public Person toPerson() {
        return new Person.Builder()
            .withId(extractIdUri())
            .withIdentifiers(extractIdentifiers())
            .withNames(extractNames())
            .withContactDetails(extractContactDetails())
            .withImage(extractImage())
            .withAffiliations(extractAffiliations())
            .build();
    }

    private ContactDetails extractContactDetails() {
        return getTel().map(ContactDetails::new).orElse(null);
    }

    private URI extractIdUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH)
            .addChild(getCristinPersonId()).getUri();
    }

    /**
     * Transforms identifiers from Cristin into a Set.
     *
     * @return Set of transformed identifiers
     */
    private Set<TypedValue> extractIdentifiers() {
        Set<TypedValue> identifiers = new HashSet<>();

        identifiers.add(new TypedValue(CRISTIN_IDENTIFIER, getCristinPersonId()));
        getOrcid().flatMap(CristinOrcid::getId).ifPresent(id -> identifiers.add(new TypedValue(ORCID, id)));

        return identifiers;
    }

    /**
     * Transforms names from a Cristin person into a Set.
     *
     * @return Set of transformed names
     */
    private Set<TypedValue> extractNames() {
        Set<TypedValue> names = new HashSet<>();

        names.add(new TypedValue(FIRST_NAME, getFirstName()));
        names.add(new TypedValue(LAST_NAME, getSurname()));
        getFirstNamePreferred().ifPresent(id -> names.add(new TypedValue(PREFERRED_FIRST_NAME, id)));
        getSurnamePreferred().ifPresent(id -> names.add(new TypedValue(PREFERRED_LAST_NAME, id)));

        return names;
    }

    private URI extractImage() {
        return getPictureUrl().map(UriWrapper::new).map(UriWrapper::getUri).orElse(null);
    }

    private Set<Affiliation> extractAffiliations() {
        return getAffiliations().stream().map(CristinAffiliation::toAffiliation).collect(
            Collectors.toSet());
    }
}
