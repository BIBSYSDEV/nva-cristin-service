package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.utils.UriUtils;

public class PersonPatchFieldFilter {

    private final transient ObjectNode input;

    /**
     * Class For filtering out fields that cannot be updated by the current user.
     */
    public PersonPatchFieldFilter(ObjectNode input) {
        this.input = input;
    }

    /**
     * Filters on given institution number. Payload needs to be validated first to verify it contains valid data.
     * Automatically filters out data that cannot be updated without instNr if supplied instNr is null.
     */
    public PersonPatchFieldFilter filterOnInstNr(String instNr) {
        if (input.has(NVI) && !input.get(NVI).isNull()) {
            var inputInstNr = parseVerifiedAtIdIntoInstNrString();

            if (missingInstNrForComparing(instNr) || instNumbersDontMatch(instNr, inputInstNr)) {
                input.remove(NVI);
            }
        }

        return this;
    }

    private String parseVerifiedAtIdIntoInstNrString() {
        return attempt(() -> input.get(NVI).get(PersonNvi.VERIFIED_AT).get(ID).asText())
                   .map(URI::create)
                   .map(UriUtils::extractLastPathElement)
                   .map(Utils::removeUnitPartFromIdentifierIfPresent)
                   .orElseThrow();
    }

    private static boolean missingInstNrForComparing(String instNr) {
        return Objects.isNull(instNr);
    }

    private static boolean instNumbersDontMatch(String instNr, String inputInstNr) {
        return !Objects.equals(inputInstNr, instNr);
    }

    public ObjectNode getFiltered() {
        return input;
    }
}
