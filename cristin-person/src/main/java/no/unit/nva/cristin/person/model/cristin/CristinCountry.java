package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.CODE;
import static no.unit.nva.cristin.model.JsonPropertyNames.CODE_ALPHA_3;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.person.model.nva.Country;

public record CristinCountry(@JsonProperty(CODE) String code,
                             @JsonProperty(CODE_ALPHA_3) String codeAlpha3,
                             @JsonProperty(NAME) Map<String, String> name) implements JsonSerializable {

    public Country toCountry() {
        return new Country(code, codeAlpha3, name);
    }

}
