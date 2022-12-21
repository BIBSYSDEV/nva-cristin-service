package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class CristinBiobank {
    //Here be more params
    private static final String CODE_FIELD = "code";
    private static final String NAME_FIELD = "name";

    @JsonProperty(CODE_FIELD)
    private final String code;
    @JsonProperty(NAME_FIELD)
    private final Map<String, String> name;


    public CristinBiobank(@JsonProperty(CODE_FIELD) String code,
                          @JsonProperty(NAME_FIELD) Map<String, String> name) {
        this.code = code;
        this.name  = Collections.unmodifiableMap(name);;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }

}
