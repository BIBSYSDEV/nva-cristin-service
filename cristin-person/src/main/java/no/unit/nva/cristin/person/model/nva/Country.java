package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.LABEL;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE_ALPHA_3;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;

public record Country(@JsonProperty(TYPE) String type,
                      @JsonProperty(TYPE_ALPHA_3) String typeAlpha3,
                      @JsonProperty(LABEL) Map<String, String> label) implements JsonSerializable {

}
