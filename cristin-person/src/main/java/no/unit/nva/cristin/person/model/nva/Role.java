package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("PMD.ShortClassName")
@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record Role(@JsonProperty("labels") Map<String, String> labels) {

    @Override
    public Map<String, String> labels() {
        return nonEmptyOrDefault(labels);
    }

}
