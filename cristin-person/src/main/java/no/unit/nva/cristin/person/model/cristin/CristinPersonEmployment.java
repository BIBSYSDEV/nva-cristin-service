package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.person.employment.Constants.EMPLOYMENT_PATH;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.model.Organization;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPersonEmployment {

    @JsonIgnore
    public static final String POSITION = "position";
    @JsonIgnore
    public static final String HASHTAG = "#";
    @JsonIgnore
    public static final String SLASH_DELIMITER = "/";

    private String id;
    private CristinOrganization affiliation;
    private Boolean active;
    private CristinPositionCode position;
    private Instant startDate;
    private Instant endDate;
    private Double ftePercentage;
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CristinOrganization getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(CristinOrganization affiliation) {
        this.affiliation = affiliation;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public CristinPositionCode getPosition() {
        return position;
    }

    public void setPosition(CristinPositionCode position) {
        this.position = position;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Double getFtePercentage() {
        return ftePercentage;
    }

    public void setFtePercentage(Double ftePercentage) {
        this.ftePercentage = ftePercentage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Creates Employment model using person identifier and this object's data.
     */
    public Employment toEmployment(String personId) {
        return new Employment.Builder()
            .withId(generateIdUriFromIdentifier(personId))
            .withType(generateTypeUri())
            .withOrganization(extractOrganizationUri())
            .withStartDate(getStartDate())
            .withEndDate(getEndDate())
            .withFullTimeEquivalentPercentage(getFtePercentage())
            .build();
    }

    private URI generateIdUriFromIdentifier(String personId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA)
            .addChild(personId).addChild(EMPLOYMENT_PATH).addChild(getId()).getUri();
    }

    private URI generateTypeUri() {
        URI uri = new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).getUri();
        return createUriWithUnescapedHashtagPath(uri);
    }

    private URI createUriWithUnescapedHashtagPath(URI uri) {
        String positionCode = getPosition().getCode();
        return attempt(() -> new URI(uri + SLASH_DELIMITER + POSITION + HASHTAG + positionCode)).orElseThrow();
    }

    private URI extractOrganizationUri() {
        return Optional.ofNullable(getAffiliation().extractPreferredTypeOfOrganization())
            .map(Organization::getId)
            .orElse(null);
    }
}
