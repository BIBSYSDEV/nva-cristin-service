package no.unit.nva.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalSourcesBiobank {


    public static final String EXTERNAL_SOURCE_SHORT_NAME = "sourceShortName";
    public static final String EXTERNAL_SOURCE_REDERENCE_ID = "sourceReferenceId";

    @JsonProperty(EXTERNAL_SOURCE_REDERENCE_ID)
    private final String sourceReferenceId;
    @JsonProperty(EXTERNAL_SOURCE_SHORT_NAME)
    private final String sourceShortName;


    public ExternalSourcesBiobank(String sourceReferenceId, String sourceShortName) {
        this.sourceReferenceId = sourceReferenceId;
        this.sourceShortName = sourceShortName;
    }

    public String getSourceReferenceId() {
        return sourceReferenceId;
    }

    public String getSourceShortName() {
        return sourceShortName;
    }
}
