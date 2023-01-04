package no.unit.nva.biobank.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Collections;
import no.unit.nva.cristin.biobank.model.nva.Biobank;
import java.util.List;


@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public final class Biobanks {
    private static final String BIOBANKS_STRING = "biobanks";

    @JsonProperty(CONTEXT)
    private final URI context;
    @JsonProperty(ID)
    private final URI id;
    @JsonProperty(BIOBANKS_STRING)
    private final List<Biobank> biobankList;

    @JsonCreator
    public Biobanks(@JsonProperty(CONTEXT) URI context,
                          @JsonProperty(ID) URI id,
                          @JsonProperty(BIOBANKS_STRING) List<Biobank> biobanks) {
        this.context = context;
        this.id = id;
        this.biobankList = Collections.unmodifiableList(biobanks);
    }

    public URI getContext() {
        return context;
    }

    public URI getId() {
        return id;
    }

    public List<Biobank> getBiobankList() {
        return biobankList;
    }
}