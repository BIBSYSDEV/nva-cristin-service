package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER_ALPHA_3;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;

public record Country(@JsonProperty(TYPE) String type,
                      @JsonProperty(IDENTIFIER) String identifier,
                      @JsonProperty(IDENTIFIER_ALPHA_3) String identifierAlpha3,
                      @JsonProperty(LABELS) Map<String, String> labels) implements JsonSerializable {

    public static final String COUNTRY_TYPE = "Country";

    @Override
    public String toString() {
        return toJsonString();
    }

}
