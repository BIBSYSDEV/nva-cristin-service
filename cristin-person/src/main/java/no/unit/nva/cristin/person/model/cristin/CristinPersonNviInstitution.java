package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACRONYM;
import static no.unit.nva.cristin.model.JsonPropertyNames.COUNTRY;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.model.Organization;

public record CristinPersonNviInstitution(@JsonProperty(ACRONYM) String acronym,
                                          @JsonProperty(COUNTRY) String country,
                                          @JsonProperty(CORRESPONDING_UNIT) CristinUnit correspondingUnit,
                                          @JsonProperty(INSTITUTION_NAME) Map<String, String> institutionName) {

    public static final String CORRESPONDING_UNIT = "corresponding_unit";
    public static final String INSTITUTION_NAME = "institution_name";

    public Organization toOrganization() {
        return new Organization.Builder()
                   .withId(getNvaApiId(getCorrespondingUnitIdentifier(), ORGANIZATION_PATH))
                   .withAcronym(acronym)
                   .withCountry(country)
                   .withLabels(institutionName)
                   .build();
    }

    private String getCorrespondingUnitIdentifier() {
        return Optional.ofNullable(correspondingUnit).map(CristinUnit::getCristinUnitId).orElse(null);
    }

}
