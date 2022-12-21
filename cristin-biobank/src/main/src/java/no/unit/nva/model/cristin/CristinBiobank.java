package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class CristinBiobank {
    //Here be more params
    private static final String CODE_FIELD = "code";

    @JsonProperty(CODE_FIELD)
    private final String code;


    public CristinBiobank(@JsonProperty(CODE_FIELD) String code) {
        this.code = code;

    }

    public String getCode() {
        return code;
    }

}
