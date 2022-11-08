package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinExternalSource {

    public static final String CRISTIN_SOURCE_SHORT_NAME = "source_short_name";
    public static final String CRISTIN_SOURCE_REFERENCE_ID = "source_reference_id";

    @JsonProperty(CRISTIN_SOURCE_SHORT_NAME)
    private final transient String sourceShortName;
    @JsonProperty(CRISTIN_SOURCE_REFERENCE_ID)
    private final transient String sourceReferenceId;

    @JsonCreator
    public CristinExternalSource(@JsonProperty(CRISTIN_SOURCE_SHORT_NAME) String sourceShortName,
                                 @JsonProperty(CRISTIN_SOURCE_REFERENCE_ID) String sourceReferenceId) {
        this.sourceShortName = sourceShortName;
        this.sourceReferenceId = sourceReferenceId;
    }

    public String getSourceShortName() {
        return sourceShortName;
    }

    public String getSourceReferenceId() {
        return sourceReferenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinExternalSource)) {
            return false;
        }
        CristinExternalSource that = (CristinExternalSource) o;
        return Objects.equals(sourceShortName, that.sourceShortName) && Objects.equals(
            sourceReferenceId, that.sourceReferenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceShortName, sourceReferenceId);
    }

}
