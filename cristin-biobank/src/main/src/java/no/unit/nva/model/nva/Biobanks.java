package no.unit.nva.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import no.unit.nva.cristin.biobank.model.nva.Biobank;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public final class Biobanks {
    private static final String BIOBANKS = "biobanks";

    @JsonProperty(CONTEXT)
    private final URI context;
    @JsonProperty(ID)
    private final URI id;
    @JsonProperty(BIOBANKS)
    private final List<no.unit.nva.cristin.biobank.model.nva.Biobank> biobanks;

    @JsonCreator
    public Biobanks(@JsonProperty(CONTEXT) URI context,
                          @JsonProperty(ID) URI id,
                          @JsonProperty(BIOBANKS) List<Biobank> biobanks) {
        this.context = context;
        this.id = id;
        this.biobanks = Collections.unmodifiableList(biobanks);
    }

    public URI getContext() {
        return context;
    }

    public URI getId() {
        return id;
    }

    public List<Biobank> getBiobanks() {
        return biobanks;
    }
}