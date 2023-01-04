package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinExternalSourcesBiobank {

    public static final String CRISTIN_EXTERNAL_SOURCE_SHORT_NAME = "source_short_name";
    public static final String CRISTIN_EXTERNAL_SOURCE_REDERENCE_ID = "source_reference_id";

    @JsonProperty(CRISTIN_EXTERNAL_SOURCE_REDERENCE_ID)
    private final String sourceReferenceId;
    @JsonProperty(CRISTIN_EXTERNAL_SOURCE_SHORT_NAME)
    private final String sourceShortName;


    public CristinExternalSourcesBiobank(String sourceReferenceId, String sourceShortName) {
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
