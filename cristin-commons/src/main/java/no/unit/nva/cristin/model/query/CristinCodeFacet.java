package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CristinCodeFacet extends CristinFacet {

    private final String code;
    private final Map<String, String> name;

    public CristinCodeFacet(@JsonProperty("code") String code,
                            @JsonProperty("name") Map<String, String> name) {
        super();
        this.code = code;
        this.name = name;
    }

    @Override
    public String getKey() {
        return code;
    }

    @Override
    public Map<String, String> getLabels() {
        return name;
    }

}
