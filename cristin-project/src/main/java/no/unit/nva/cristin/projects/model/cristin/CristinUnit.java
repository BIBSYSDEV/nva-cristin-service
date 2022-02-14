package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinUnit {

    @JsonIgnore
    private static final Pattern CRISTIN_UNIT_IDENTIFIER = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);

    private String cristinUnitId;
    private Map<String, String> unitName;
    private String url;

    public String getCristinUnitId() {
        return cristinUnitId;
    }

    public void setCristinUnitId(String cristinUnitId) {
        this.cristinUnitId = cristinUnitId;
    }

    public Map<String, String> getUnitName() {
        return unitName;
    }

    public void setUnitName(Map<String, String> unitName) {
        this.unitName = unitName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Creates an Organization model based on this CristinUnit model.
     *
     * @return Organization model
     */
    public Organization toOrganization() {
        URI id = getNvaApiId(getCristinUnitId(), ORGANIZATION_PATH);
        return new Organization.Builder().withId(id).withName(getUnitName()).build();
    }

    public static CristinUnit fromCristinUnitIdentifier(String unitIdentifier) {
        CristinUnit cristinUnit = new CristinUnit();
        cristinUnit.setCristinUnitId(unitIdentifier);
        return cristinUnit;
    }

    public static Optional<String> extractUnitIdentifier(Organization organization) {
        return Optional.of(organization)
            .map(Organization::getId).map(UriUtils::extractLastPathElement)
            .filter(identifier -> CRISTIN_UNIT_IDENTIFIER.matcher(identifier).matches());
    }
}

