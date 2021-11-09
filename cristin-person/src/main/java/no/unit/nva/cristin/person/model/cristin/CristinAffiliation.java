package no.unit.nva.cristin.person.model.cristin;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.cristin.person.model.nva.Affiliation;
import no.unit.nva.cristin.person.model.nva.Role;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinAffiliation {

    private Boolean active;
    private Map<String, String> position;
    private CristinUnit unit;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Map<String, String> getPosition() {
        return Objects.nonNull(position) ? position : Collections.emptyMap();
    }

    public void setPosition(Map<String, String> position) {
        this.position = position;
    }

    public CristinUnit getUnit() {
        return unit;
    }

    public void setUnit(CristinUnit unit) {
        this.unit = unit;
    }

    /**
     * Creates an Affiliation from a CristinAffiliation.
     *
     * @return The transformed Cristin model.
     */
    public Affiliation toAffiliation() {
        URI organization = attempt(() -> new URI(getUnit().getUrl())).orElse(uriFailure -> null);
        return new Affiliation(organization, getActive(), extractAffiliationRole());
    }

    private Role extractAffiliationRole() {
        URI uri = attempt(() -> new URI("https://example.org/link/to/ontology#1026")).orElseThrow();
        return new Role(uri, getPosition());
    }
}
