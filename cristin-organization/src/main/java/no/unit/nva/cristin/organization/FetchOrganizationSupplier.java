package no.unit.nva.cristin.organization;

import static no.unit.nva.cristin.model.Constants.PARENT_UNIT_ID;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.LEVELS;
import static no.unit.nva.cristin.organization.CristinOrganizationApiClient.ALL_SUB_LEVELS;
import static no.unit.nva.cristin.organization.CristinOrganizationApiClient.FIRST_LEVEL;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import java.net.URI;
import java.util.Map;

public class FetchOrganizationSupplier {

    public URI uriForSubUnits(String identifier, boolean allLevels) {
        var levels = allLevels ? ALL_SUB_LEVELS : FIRST_LEVEL;
        return createCristinQueryUri(queryParamsForSubUnits(identifier, levels), UNITS_PATH);
    }

    private Map<String, String> queryParamsForSubUnits(String identifier, String depth) {
        return Map.of(PARENT_UNIT_ID, identifier,
                      LEVELS, depth);
    }

}
