package no.unit.nva.cristin.funding.sources.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;

public final class CristinFundingSource {
    private static final String CODE_FIELD = "code";
    private static final String NAME_FIELD = "name";

    @JsonProperty(CODE_FIELD)
    private final String code;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> name;

    public CristinFundingSource(@JsonProperty(CODE_FIELD) String code,
                                @JsonProperty(NAME_FIELD) Map<String, String> name) {
        this.code = code;
        this.name = Collections.unmodifiableMap(name);
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }
}
