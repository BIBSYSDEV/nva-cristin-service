package no.unit.nva.cristin.funding.sources.model.cristin;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

// The private fields in the record are viewed as unused, this is a bug
@SuppressWarnings("PMD.UnusedPrivateField")
public record CristinFundingSource(@JsonProperty(CODE_FIELD) String code,
                                   @JsonProperty(NAME_FIELD) Map<String, String> name) {

    private static final String CODE_FIELD = "code";
    private static final String NAME_FIELD = "name";

    @Override
    public Map<String, String> name() {
        return nonEmptyOrDefault(name);
    }
}
