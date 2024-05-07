package no.unit.nva.cristin.person.orcid.model;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.HITS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.SIZE;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.List;
import no.unit.nva.commons.json.JsonSerializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public record PersonsOrcid(@JsonProperty(CONTEXT) URI context,
                           @JsonProperty(ID) URI id,
                           @JsonProperty(SIZE) int size,
                           @JsonProperty(HITS) List<PersonOrcid> hits) implements JsonSerializable {

    public static final URI PERSON_ORCID_CONTEXT = URI.create("https://example.org/persons-orcid-context.json");
    public static final URI PERSON_ORCID_ID = URI.create("https://api.dev.nva.aws.unit.no/cristin/person/orcid");

    public PersonsOrcid(@JsonProperty(SIZE) int size,
                        @JsonProperty(HITS) List<PersonOrcid> hits) {
        this(PERSON_ORCID_CONTEXT, PERSON_ORCID_ID, size, hits);
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
