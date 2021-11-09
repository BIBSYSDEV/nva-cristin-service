package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.person.Constants.BASE_PATH;
import static no.unit.nva.cristin.person.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.person.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.cristin.person.model.nva.Affiliation;
import no.unit.nva.cristin.person.model.nva.ContactDetails;
import no.unit.nva.cristin.person.model.nva.NvaIdentifier;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPerson {

    @JsonIgnore
    public static final String PERSON_PATH = "person";
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

    public CristinOrcid getOrcid() {
        return orcid;
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

    public String getFirstNamePreferred() {
        return firstNamePreferred;
    }

    public void setFirstNamePreferred(String firstNamePreferred) {
        this.firstNamePreferred = firstNamePreferred;
    }

    public String getSurnamePreferred() {
        return surnamePreferred;
    }

    public void setSurnamePreferred(String surnamePreferred) {
        this.surnamePreferred = surnamePreferred;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPictureUrl() {
        return pictureUrl;
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
            .withContactDetails(new ContactDetails(getTel()))
            .withImage(extractImage())
            .withAffiliations(extractAffiliations())
            .build();
    }

    private URI extractIdUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH)
            .addChild(getCristinPersonId()).getUri();
    }

    /**
     * Transforms identifiers from Cristin into a List.
     *
     * @return List of transformed identifiers
     */
    private List<NvaIdentifier> extractIdentifiers() {
        List<NvaIdentifier> identifiers = new ArrayList<>();

        Optional.ofNullable(getCristinPersonId())
            .ifPresent(id -> identifiers.add(new NvaIdentifier(CRISTIN_IDENTIFIER, id)));
        Optional.ofNullable(getOrcid())
            .map(CristinOrcid::getId)
            .ifPresent(id -> identifiers.add(new NvaIdentifier(ORCID, id)));

        return identifiers;
    }

    /**
     * Transforms names from a Cristin person into a List.
     *
     * @return List of transformed names
     */
    private List<NvaIdentifier> extractNames() {
        List<NvaIdentifier> names = new ArrayList<>();

        Optional.ofNullable(getFirstName())
            .ifPresent(id -> names.add(new NvaIdentifier(FIRST_NAME, id)));
        Optional.ofNullable(getSurname())
            .ifPresent(id -> names.add(new NvaIdentifier(LAST_NAME, id)));
        Optional.ofNullable(getFirstNamePreferred())
            .ifPresent(id -> names.add(new NvaIdentifier(PREFERRED_FIRST_NAME, id)));
        Optional.ofNullable(getSurnamePreferred())
            .ifPresent(id -> names.add(new NvaIdentifier(PREFERRED_LAST_NAME, id)));

        return names;
    }

    private URI extractImage() {
        return attempt(() -> new URI(getPictureUrl())).orElse(uriFailure -> null);
    }

    private List<Affiliation> extractAffiliations() {
        return getAffiliations().stream().map(CristinAffiliation::toAffiliation).collect(
            Collectors.toList());
    }
}
