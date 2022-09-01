package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.nva.Affiliation;
import no.unit.nva.cristin.person.model.nva.Role;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinAffiliation {

    private boolean active;
    @JsonProperty("position")
    private Map<String, String> roleLabel;
    private CristinUnit unit;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Map<String, String> getRoleLabel() {
        return Objects.nonNull(roleLabel) ? roleLabel : Collections.emptyMap();
    }

    public void setRoleLabel(Map<String, String> roleLabel) {
        this.roleLabel = roleLabel;
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
        return new Affiliation(extractOrganizationIdentifierUri(), isActive(), extractAffiliationRole());
    }

    private URI extractOrganizationIdentifierUri() {
        return Optional.ofNullable(getUnit())
            .map(CristinUnit::getCristinUnitId)
            .map(identifier -> getNvaApiId(identifier, ORGANIZATION_PATH))
            .orElse(null);
    }

    private Role extractAffiliationRole() {
        return new Role(getRoleLabel());
    }
}
