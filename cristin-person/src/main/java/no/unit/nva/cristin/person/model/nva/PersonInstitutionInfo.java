package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public record PersonInstitutionInfo(@JsonProperty("id") URI id,
                                    @JsonProperty("email") String email,
                                    @JsonProperty("phone") String phone) implements JsonSerializable {

    @JsonProperty(CONTEXT)
    private static final String context = "https://ontology.org";

    @JsonProperty(CONTEXT)
    public String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
