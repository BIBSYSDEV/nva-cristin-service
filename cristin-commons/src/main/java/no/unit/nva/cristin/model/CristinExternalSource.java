package no.unit.nva.cristin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.model.ExternalSource;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinExternalSource(@JsonProperty(CRISTIN_SOURCE_SHORT_NAME) String sourceShortName,
                                    @JsonProperty(CRISTIN_SOURCE_REFERENCE_ID) String sourceReferenceId) {

    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";
    public static final String CRISTIN_SOURCE_REFERENCE_ID = "source_reference_id";

    public ExternalSource toExternalSource() {
        return new ExternalSource(sourceReferenceId(), sourceShortName());
    }

    public static CristinExternalSource fromExternalSource(ExternalSource externalSource) {
        return new CristinExternalSource(externalSource.getName(), externalSource.getIdentifier());
    }

}
